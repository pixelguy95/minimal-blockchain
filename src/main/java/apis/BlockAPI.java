package apis;

import apis.domain.requests.NewBlockFoundRequest;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.BlockHeightResponse;
import apis.domain.responses.BooleanResponse;
import apis.domain.responses.GetAllBlockHashesResponse;
import apis.domain.responses.GetBlockResponse;
import apis.static_structures.Blockchain;
import apis.utils.BlockVerifier;
import domain.block.Block;
import domain.transaction.Transaction;
import node.Config;
import node.SpecialJSONSerializer;
import spark.Request;
import spark.Response;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class BlockAPI {

    private Blockchain blockchain;
    private Config config;

    public BlockAPI(Blockchain blockchain, Config config) {
        this.blockchain = blockchain;
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
     * @param request
     * @param response
     * @return
     */
    public BooleanResponse newBlockFound(Request request, Response response) {
        Block b = SpecialJSONSerializer.getInstance().fromJson(request.body(), NewBlockFoundRequest.class).block;

        if(config.verifyNewBlocks && !BlockVerifier.verifyBlock(b)) {
            return (BooleanResponse) new BooleanResponse().setError("Block didn't pass verification");
        }

        blockchain.addBlock(b);
        System.out.println("ADDED " + Base64.getUrlEncoder().withoutPadding().encodeToString(b.header.getHash()));
        //TODO: RETRANSMIT

        return new BooleanResponse();
    }

    public String retransmittedBlock(Request request, Response response) {

        return "Implement me!";
    }

    public GetAllBlockHashesResponse getAllBlockHashes(Request request, Response response) {

        List<String> hashes = blockchain.getChain().values().stream()
                .map(storedBlock->storedBlock.blockHeader.getHash())
                .map(hash->Base64.getUrlEncoder().withoutPadding().encodeToString(hash))
                .collect(Collectors.toList());

        return new GetAllBlockHashesResponse(hashes);
    }
}
