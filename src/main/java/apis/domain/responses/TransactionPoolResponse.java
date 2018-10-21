package apis.domain.responses;

import domain.transaction.Transaction;

import java.util.List;

public class TransactionPoolResponse extends Response {
    List<Transaction> transactionPool;

    public TransactionPoolResponse(List<Transaction> transactionPool) {
        this.transactionPool = transactionPool;
    }
}
