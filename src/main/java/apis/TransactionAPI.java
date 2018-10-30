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
import apis.utils.TransactionValidator;
import apis.utils.TransactionVerifier;
import domain.transaction.Transaction;
import node.Config;
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
    private TransactionValidator transactionValidator;
    private Config config;

    public TransactionAPI(TransactionPool transactionPool, KnownNodesList knownNodesList, TransactionValidator transactionValidator, Config config) {
        this.transactionPool = transactionPool;
        this.knownNodesList = knownNodesList;
        this.transactionValidator = transactionValidator;
        this.config = config;
    }

    public NewTransactionResponse newTransaction(Request request, Response response) {
        Transaction t = SpecialJSONSerializer.getInstance().fromJson(request.body(), NewTransactionRequest.class).transaction;

        if(config.verifyTransactions) {

            TransactionValidator.Result res = transactionValidator.validate(t);
            if(res.passed) {
                transactionPool.put(t);
                retransmitTransaction(t.fullHash());
            } else {
                return (NewTransactionResponse) new NewTransactionResponse().setError("Transaction didn't pass validation");
            }

        } else {
            transactionPool.put(t);
            retransmitTransaction(t.fullHash());
        }

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
                if(config.verifyTransactions) {

                    TransactionValidator.Result res = transactionValidator.validate(gtr.transaction);
                    if(res.passed) {
                        transactionPool.put(gtr.transaction);
                        retransmitTransaction(gtr.transaction.fullHash());
                    } else {
                        return (TransactionRetransmissionResponse) new TransactionRetransmissionResponse().setError("Transaction didn't pass validation");
                    }

                } else {
                    transactionPool.put(gtr.transaction);
                    retransmitTransaction(gtr.transaction.fullHash());
                }
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
