package apis;

import apis.domain.Host;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.GetTransactionResponse;
import apis.domain.responses.NewTransactionResponse;
import apis.domain.responses.TransactionRetransmissionResponse;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import apis.utils.BlockRESTWrapper;
import apis.utils.TransactionRESTWrapper;
import apis.utils.TransactionVerifier;
import domain.transaction.Transaction;
import node.SpecialJSONSerializer;
import org.restlet.resource.ResourceException;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TransactionAPI {

    private TransactionPool transactionPool;
    private KnownNodesList knownNodesList;

    public TransactionAPI(TransactionPool transactionPool, KnownNodesList knownNodesList) {
        this.transactionPool = transactionPool;
        this.knownNodesList = knownNodesList;
    }

    public NewTransactionResponse newTransaction(Request request, Response response) {
        Transaction t = SpecialJSONSerializer.getInstance().fromJson(request.body(), NewTransactionRequest.class).transaction;

        if (!TransactionVerifier.verifyTransaction(t)) {
            return (NewTransactionResponse) new NewTransactionResponse().setError("Transaction didn't pass validation");
        }

        transactionPool.put(t);
        retransmitTransaction(t.fullHash());

        return new NewTransactionResponse();
    }

    public GetTransactionResponse fetchTransaction(Request request, Response response) {
        Transaction t = transactionPool.get(Base64.getUrlDecoder().decode(request.params("txid")));

        if (t != null) {
            return new GetTransactionResponse(t);
        }

        return (GetTransactionResponse) new GetTransactionResponse(null).setError("No transaction with that txid: " + request.params("txid"));
    }

    public TransactionRetransmissionResponse retransmittedTransaction(Request request, Response response) {

        byte[] txid = Base64.getUrlDecoder().decode(request.params("txid"));

        if (transactionPool.has(txid)) {
            return (TransactionRetransmissionResponse) new TransactionRetransmissionResponse().setError("This transaction has already been transmitted");
        }

        List<Host> potentialHolders = knownNodesList.getAllNodesUnderIP(request.ip());

        for (Host h : potentialHolders) {
            GetTransactionResponse gtr = TransactionRESTWrapper.getTransaction(h, txid);

            if (!gtr.error) {
                transactionPool.put(gtr.transaction);
                break;
            }
        }

        retransmitTransaction(txid);

        return new TransactionRetransmissionResponse();
    }

    private void retransmitTransaction(byte[] hash) {
        List<Host> notResponding = new ArrayList<>();
        knownNodesList.getKnownNodes().stream().forEach(host -> {
            try {
                TransactionRESTWrapper.retransmitTransaction(host, hash);
            } catch (ResourceException e) {
                notResponding.add(host);
            }
        });

        notResponding.forEach(h->knownNodesList.removeNode(h));
    }
}
