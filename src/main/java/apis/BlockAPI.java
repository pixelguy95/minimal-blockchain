package apis;

import apis.domain.Host;
import apis.domain.requests.NewBlockFoundRequest;
import apis.domain.responses.*;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import apis.utils.*;
import apis.utils.validators.BlockValidator;
import apis.utils.validators.TransactionValidator;
import apis.utils.validators.Validator;
import apis.utils.wrappers.BlockRESTWrapper;
import domain.block.Block;
import domain.block.StoredBlock;
import node.Config;
import node.SpecialJSONSerializer;
import spark.Request;
import spark.Response;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class BlockAPI {

    private Blockchain blockchain;
    private UTXO utxo;
    private KnownNodesList knownNodesList;
    private TransactionPool transactionPool;
    private Config config;

    private TransactionValidator transactionValidator;
    private BlockValidator blockValidator;

    public BlockAPI(Blockchain blockchain,
                    UTXO utxo,
                    TransactionPool transactionPool,
                    BlockValidator blockValidator,
                    TransactionValidator transactionValidator,
                    KnownNodesList knownNodesList,
                    Config config) {
        this.blockchain = blockchain;
        this.utxo = utxo;
        this.transactionPool = transactionPool;
        this.knownNodesList = knownNodesList;
        this.transactionValidator = transactionValidator;
        this.blockValidator = blockValidator;
        this.config = config;
    }

    public BlockHeightResponse getCurrentBlockHeight(Request request, Response response) {
        return new BlockHeightResponse(new BigInteger(String.valueOf(blockchain.getBestHeight())));
    }

    public GetBlockResponse getBlock(Request request, Response response) {

        byte[] blockhash = Base64.getUrlDecoder().decode(request.params("blockhash"));
        if (!blockchain.getChain().containsKey(ByteBuffer.wrap(blockhash))) {
            return (GetBlockResponse) new GetBlockResponse(null).setError("No such block " + request.params("blockhash"));
        }

        return new GetBlockResponse(blockchain.getBlock(blockhash));
    }

    /**
     * This is where new blocks have to be validate and added to the blockchain. If valid retransmit and add to blockchain.
     * Ask the blockchain for new valid utxos and add them to the db.
     *
     * @param request
     * @param response
     * @return
     */
    public BooleanResponse newBlockFound(Request request, Response response) {
        Block b = SpecialJSONSerializer.getInstance().fromJson(request.body(), NewBlockFoundRequest.class).block;

        if (config.validateNewBlocks) {

            Validator.Result result = blockValidator.validate(b);

            if(result.passed) {
                BlockAddingManager.addBlockAndManageUTXOs(blockchain, utxo, transactionPool, transactionValidator, config, b);
                retransmitBlockHashToAll(b.header.getHash());
            } else {
                System.err.println(result.resaon);
                return (BooleanResponse) new BooleanResponse().setError("Block didn't pass verification: " + result.resaon);
            }

        } else {
            BlockAddingManager.addBlockAndManageUTXOs(blockchain, utxo, transactionPool, transactionValidator, config, b);
            retransmitBlockHashToAll(b.header.getHash());
        }

        return new BooleanResponse();
    }

    public BooleanResponse retransmittedBlock(Request request, Response response) {

        byte[] blockhash = Base64.getUrlDecoder().decode(request.params("blockhash"));

        if (blockchain.getChain().containsKey(ByteBuffer.wrap(blockhash)))
            return (BooleanResponse) new BooleanResponse().setError("Already have that block");

        List<Host> potentialHolders = knownNodesList.getAllNodesUnderIP(request.ip());

        for (Host h : potentialHolders) {
            GetBlockResponse gbr = BlockRESTWrapper.getBlock(h, blockhash);

            if (!gbr.error) {
                if (config.validateNewBlocks) {
                    Validator.Result result = blockValidator.validate(gbr.block);

                    if(result.passed) {
                        BlockAddingManager.addBlockAndManageUTXOs(blockchain, utxo, transactionPool, transactionValidator, config, gbr.block);
                        retransmitBlockHashToAll(gbr.block.header.getHash());
                    } else {
                        System.err.println(result.resaon);
                        return (BooleanResponse) new BooleanResponse().setError("Block didn't pass verification: " + result.resaon);
                    }

                } else {
                    BlockAddingManager.addBlockAndManageUTXOs(blockchain, utxo, transactionPool, transactionValidator, config, gbr.block);
                    retransmitBlockHashToAll(gbr.block.header.getHash());
                }
            }
        }

        retransmitBlockHashToAll(blockhash);
        return new BooleanResponse();
    }

    public GetAllBlockHashesResponse getAllBlockHashes(Request request, Response response) {

        List<String> hashes = blockchain.getChain().values().stream()
                .sorted(Comparator.comparing(StoredBlock::height))
                .map(storedBlock -> storedBlock.blockHeader.getHash())
                .map(hash -> Base64.getUrlEncoder().withoutPadding().encodeToString(hash))
                .collect(Collectors.toList());

        return new GetAllBlockHashesResponse(hashes);
    }

    public GetAllBlockHashesResponse getAllBlockHashesFromHeight(Request request, Response response) {
        int height = Integer.parseInt(request.params("height"));

        List<String> hashes = blockchain.getChain().values().stream()
                .filter(storedBlock -> storedBlock.height >= height)
                .sorted(Comparator.comparing(StoredBlock::height))
                .map(storedBlock -> storedBlock.blockHeader.getHash())
                .map(hash -> Base64.getUrlEncoder().withoutPadding().encodeToString(hash))
                .collect(Collectors.toList());

        return new GetAllBlockHashesResponse(hashes);
    }

    private void retransmitBlockHashToAll(byte[] hash) {
        List<Host> notResponding = new ArrayList<>();

        new ArrayList<>(knownNodesList.getKnownNodes()).stream().forEach(host -> {
            try {
                System.out.println(host.ip + " " + host.port);
                BlockRESTWrapper.retransmitBlock(host, hash);
            } catch (Exception e) {
                notResponding.add(host);
            }
        });

        notResponding.forEach(h -> knownNodesList.removeNode(h));
    }
}
