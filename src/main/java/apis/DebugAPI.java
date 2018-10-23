package apis;

import apis.domain.responses.TransactionPoolIDsResponse;
import apis.domain.responses.TransactionPoolResponse;
import apis.static_structures.TransactionPool;
import spark.Request;
import spark.Response;

import java.util.Base64;
import java.util.stream.Collectors;

public class DebugAPI {

    private TransactionPool transactionPool;

    public DebugAPI(TransactionPool transactionPool) {
        this.transactionPool = transactionPool;
    }

    public TransactionPoolResponse getEntireTransactionPool(Request request, Response response) {
        return  new TransactionPoolResponse(transactionPool.getAll());
    }

    public TransactionPoolIDsResponse getEntireTransactionPoolIDs(Request request, Response response) {
        return  new TransactionPoolIDsResponse(transactionPool.getAll().stream()
                .map(t->t.fullHash())
                .map(b-> Base64.getUrlEncoder().withoutPadding().encodeToString(b))
                .collect(Collectors.toList()));
    }
}
