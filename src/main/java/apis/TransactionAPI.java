package apis;

import apis.domain.requests.AddrRequest;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.GetTransactionResponse;
import apis.domain.responses.NewTransactionResponse;
import apis.domain.responses.TransactionRetransmissionResponse;
import apis.static_structures.TransactionPool;
import apis.utils.TransactionVerifier;
import com.google.gson.Gson;
import domain.transaction.Transaction;
import spark.Request;
import spark.Response;

import java.util.Base64;

public class TransactionAPI {
    public static NewTransactionResponse newTransaction(Request request, Response response) {
        Transaction t = new Gson().fromJson(request.body(), NewTransactionRequest.class).transaction;

        if(!TransactionVerifier.verifyTransaction(t)) {
            return (NewTransactionResponse) new NewTransactionResponse().setError("Transaction didn't pass validation");
        }

        TransactionPool.getInstance().put(t);

        //TODO: Transmit the transaction-id (the hash) to other nodes

        return new NewTransactionResponse();
    }

    public static GetTransactionResponse fetchTransaction(Request request, Response response) {
        return new GetTransactionResponse(TransactionPool.getInstance().get(Base64.getUrlDecoder().decode(request.params("txid"))));
    }

    public static TransactionRetransmissionResponse retransmittedTransaction(Request request, Response response) {

        return null;
    }
}
