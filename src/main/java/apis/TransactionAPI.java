package apis;

import apis.domain.Host;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.GetTransactionResponse;
import apis.domain.responses.NewTransactionResponse;
import apis.domain.responses.TransactionRetransmissionResponse;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import apis.utils.TransactionRESTWrapper;
import apis.utils.TransactionVerifier;
import com.google.gson.Gson;
import domain.transaction.Transaction;
import spark.Request;
import spark.Response;

import java.util.Base64;
import java.util.List;

public class TransactionAPI {
    public static NewTransactionResponse newTransaction(Request request, Response response) {
        Transaction t = new Gson().fromJson(request.body(), NewTransactionRequest.class).transaction;

        if (!TransactionVerifier.verifyTransaction(t)) {
            return (NewTransactionResponse) new NewTransactionResponse().setError("Transaction didn't pass validation");
        }

        TransactionPool.getInstance().put(t);

        KnownNodesList.getKnownNodes().stream().forEach(node -> {
            TransactionRESTWrapper.retransmitTransaction(node, t.fullHash());
        });

        return new NewTransactionResponse();
    }

    public static GetTransactionResponse fetchTransaction(Request request, Response response) {
        Transaction t = TransactionPool.getInstance().get(Base64.getUrlDecoder().decode(request.params("txid")));

        if (t != null) {
            return new GetTransactionResponse(t);
        }

        return (GetTransactionResponse) new GetTransactionResponse(null).setError("No transaction with that txid: " + request.params("txid"));
    }

    public static TransactionRetransmissionResponse retransmittedTransaction(Request request, Response response) {

        byte[] txid = Base64.getUrlDecoder().decode(request.params("txid"));

        if (TransactionPool.getInstance().has(txid)) {
            return (TransactionRetransmissionResponse) new TransactionRetransmissionResponse().setError("This transaction has already been transmitted");
        }

        List<Host> potentialHolders = KnownNodesList.getAllNodesUnderIP(request.ip());

        for (Host h : potentialHolders) {
            GetTransactionResponse gtr = TransactionRESTWrapper.getTransaction(h, txid);

            if (!gtr.error) {
                TransactionPool.getInstance().put(gtr.transaction);
                break;
            }
        }

        KnownNodesList.getKnownNodes().stream().forEach(node -> {
            TransactionRESTWrapper.retransmitTransaction(node, txid);
        });

        return new TransactionRetransmissionResponse();
    }
}
