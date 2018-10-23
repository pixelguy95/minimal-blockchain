package apis;

import apis.static_structures.Blockchain;
import block.Block;
import spark.Request;
import spark.Response;

import java.math.BigInteger;

public class BlockAPI {

    private Blockchain blockchain;

    public BlockAPI(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public BigInteger getCurrentBlockHeight(Request request, Response response) {

        return new BigInteger(String.valueOf(blockchain.getBestHeight()));
    }

    public Block getBlock(Request request, Response response) {

        return null;
    }

    /**
     * This is where new blocks have to be validate and added to the blockchain. If valid retransmit and add to blockchain.
     * @param request
     * @param response
     * @return
     */
    public String newBlockFound(Request request, Response response) {

        return null;
    }
}
