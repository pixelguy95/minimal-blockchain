package apis;

import block.Block;
import spark.Request;
import spark.Response;

import java.math.BigInteger;

public class BlockAPI {
    public static BigInteger getCurrentBlockHeight(Request request, Response response) {

        return null;
    }

    public static Block getBlock(Request request, Response response) {

        return null;
    }

    /**
     * This is where new blocks have to be validate and added to the blockchain. If valid retransmit and add to blockchain.
     * @param request
     * @param response
     * @return
     */
    public static String newBlockFound(Request request, Response response) {

        return null;
    }
}
