package apis.utils;

import apis.domain.Host;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.GetTransactionResponse;
import apis.domain.responses.NewTransactionResponse;
import apis.domain.responses.TransactionRetransmissionResponse;
import domain.transaction.Transaction;
import utils.RESTUtils;

import java.util.Arrays;
import java.util.Base64;

public class TransactionRESTWrapper {
    public static GetTransactionResponse getTransaction(Host host, byte[] txid) {
        return RESTUtils.get(host, "transaction", GetTransactionResponse.class, Arrays.asList(Base64.getUrlEncoder().withoutPadding().encodeToString(txid)));
    }

    public static TransactionRetransmissionResponse retransmitTransaction(Host host, byte[] txid) {
        return RESTUtils.get(host, "transaction/retransmission", TransactionRetransmissionResponse.class, Arrays.asList(Base64.getUrlEncoder().withoutPadding().encodeToString(txid)));
    }

    public static NewTransactionResponse sendTransaction(Host host, Transaction t) {
        return RESTUtils.post(host, "transaction", NewTransactionResponse.class, new NewTransactionRequest(t));
    }
}
