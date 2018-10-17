package apis.domain.requests;

import domain.transaction.Transaction;

public class NewTransactionRequest extends Request {
    public Transaction transaction;

    public NewTransactionRequest(Transaction transaction) {
        this.transaction = transaction;
    }
}
