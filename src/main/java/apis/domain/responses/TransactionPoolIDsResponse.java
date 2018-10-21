package apis.domain.responses;

import domain.transaction.Transaction;

import java.util.List;

public class TransactionPoolIDsResponse extends Response {
    List<String> transactionPoolIDs;

    public TransactionPoolIDsResponse(List<String> transactionPoolIDs) {
        this.transactionPoolIDs = transactionPoolIDs;
    }
}
