package apis;

import apis.domain.Host;
import apis.domain.requests.NewBlockFoundRequest;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.*;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import apis.utils.BlockRESTWrapper;
import apis.utils.BlockVerifier;
import apis.utils.TransactionRESTWrapper;
import apis.utils.TransactionVerifier;
import domain.block.Block;
import domain.block.StoredBlock;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.utxo.UTXOIdentifier;
import node.Config;
import node.SpecialJSONSerializer;
import org.restlet.resource.ResourceException;
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

    public BlockAPI(Blockchain blockchain, UTXO utxo, TransactionPool transactionPool, KnownNodesList knownNodesList, Config config) {
        this.blockchain = blockchain;
        this.utxo = utxo;
        this.transactionPool = transactionPool;
        this.knownNodesList = knownNodesList;
        this.config = config;
    }

    public BlockHeightResponse getCurrentBlockHeight(Request request, Response response) {
        return new BlockHeightResponse(new BigInteger(String.valueOf(blockchain.getBestHeight())));
    }

    public GetBlockResponse getBlock(Request request, Response response) {

        byte[] blockhash = Base64.getUrlDecoder().decode(request.params("blockhash"));
        if(!blockchain.getChain().containsKey(ByteBuffer.wrap(blockhash))) {
            return (GetBlockResponse) new GetBlockResponse(null).setError("No such block " + request.params("blockhash"));
        }

        return new GetBlockResponse(blockchain.getBlock(blockhash));
    }

    /**
     * This is where new blocks have to be validate and added to the blockchain. If valid retransmit and add to blockchain.
     * Ask the blockchain for new valid utxos and add them to the db.
     * @param request
     * @param response
     * @return
     */
    public BooleanResponse newBlockFound(Request request, Response response) {
        Block b = SpecialJSONSerializer.getInstance().fromJson(request.body(), NewBlockFoundRequest.class).block;

        if(config.verifyNewBlocks && !BlockVerifier.verifyBlock(b)) {
            return (BooleanResponse) new BooleanResponse().setError("Block didn't pass verification");
        }

        addNewBlockAndManageUTXO(b);

        retransmitBlockHashToAll(b.header.getHash());
        return new BooleanResponse();
    }

    public BooleanResponse retransmittedBlock(Request request, Response response) {

        byte[] blockhash = Base64.getUrlDecoder().decode(request.params("blockhash"));

        if(blockchain.getChain().containsKey(ByteBuffer.wrap(blockhash)))
            return (BooleanResponse) new BooleanResponse().setError("Already have that block");

        List<Host> potentialHolders = knownNodesList.getAllNodesUnderIP(request.ip());

        for (Host h : potentialHolders) {
            GetBlockResponse gbr = BlockRESTWrapper.getBlock(h, blockhash);

            if (!gbr.error) {
                if(config.verifyNewBlocks) {
                    if(BlockVerifier.verifyBlock(gbr.block)) {
                        addNewBlockAndManageUTXO(gbr.block);
                        break;
                    } else {
                        return (BooleanResponse) new BooleanResponse().setError("Faulty block received");
                    }
                } else {
                    addNewBlockAndManageUTXO(gbr.block);
                    break;
                }
            }
        }

        retransmitBlockHashToAll(blockhash);
        return new BooleanResponse();
    }

    public GetAllBlockHashesResponse getAllBlockHashes(Request request, Response response) {

        List<String> hashes = blockchain.getChain().values().stream()
                .sorted(Comparator.comparing(StoredBlock::height))
                .map(storedBlock->storedBlock.blockHeader.getHash())
                .map(hash->Base64.getUrlEncoder().withoutPadding().encodeToString(hash))
                .collect(Collectors.toList());

        return new GetAllBlockHashesResponse(hashes);
    }

    public GetAllBlockHashesResponse getAllBlockHashesFromHeight(Request request, Response response) {
        int height = Integer.parseInt(request.params("height"));

        List<String> hashes = blockchain.getChain().values().stream()
                .filter(storedBlock -> storedBlock.height >= height)
                .sorted(Comparator.comparing(StoredBlock::height))
                .map(storedBlock->storedBlock.blockHeader.getHash())
                .map(hash->Base64.getUrlEncoder().withoutPadding().encodeToString(hash))
                .collect(Collectors.toList());

        return new GetAllBlockHashesResponse(hashes);
    }

    private void addNewBlockAndManageUTXO(Block block) {
        blockchain.addBlock(block);

        block.transactions.stream().forEach(t->{
            for(int i = 0; i < t.inputs.size(); i++) {
                utxo.makeBusy(new UTXOIdentifier(t.inputs.get(i).transactionHash, t.inputs.get(i).outputIndex));
            }

            transactionPool.remove(t.fullHash());
        });

        blockchain.getUTXOCandidates().entrySet().stream().forEach(entry->{
            utxo.put(entry.getKey(), entry.getValue());
        });

        blockchain.getUTXORemovalCandidates().keySet().stream().forEach(key -> {
            utxo.remove(key);
        });

        List<StoredBlock> pruned = blockchain.prune();
        pruned.stream().forEach(s-> {
            Block b = blockchain.getBlock(s.blockHeader.getHash());

            b.transactions.stream().forEach(t->{

                t.inputs.stream().forEach(input-> {
                    UTXOIdentifier utxoIdentifier = new UTXOIdentifier(input.transactionHash, input.outputIndex);
                    utxo.makeUnBusy(utxoIdentifier);
                });

                if(config.verifyTransactions) {
                    if(TransactionVerifier.verifyTransaction(t)) {
                        transactionPool.put(t);
                    }
                } else {
                    transactionPool.put(t);
                }
            });
        });
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

        notResponding.forEach(h->knownNodesList.removeNode(h));
    }
}
