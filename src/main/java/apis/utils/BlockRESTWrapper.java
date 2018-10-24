package apis.utils;

import apis.domain.Host;
import apis.domain.requests.NewBlockFoundRequest;
import apis.domain.responses.*;
import domain.block.Block;
import utils.RESTUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;

public class BlockRESTWrapper {

    public static BigInteger getCurrentBlockHeight(Host host) {
        return RESTUtils.get(host, "block/height", BlockHeightResponse.class).height;
    }

    public static GetBlockResponse getBlock(Host host, byte[] hash) {
        String blockHash = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        return RESTUtils.get(host, "block", GetBlockResponse.class, Arrays.asList(blockHash));
    }

    public static GetAllBlockHashesResponse getAllBlockHashes(Host host) {
        return RESTUtils.get(host, "block/all", GetAllBlockHashesResponse.class);
    }

    public static BooleanResponse newBlock(Host host, Block b) {
        return RESTUtils.post(host, "block", BooleanResponse.class, new NewBlockFoundRequest(b));
    }

    public static TransactionRetransmissionResponse retransmitBlock(Host host, byte[] txid) {
        return RESTUtils.get(host, "block/retransmission", TransactionRetransmissionResponse.class, Arrays.asList(Base64.getUrlEncoder().withoutPadding().encodeToString(txid)));
    }
}
