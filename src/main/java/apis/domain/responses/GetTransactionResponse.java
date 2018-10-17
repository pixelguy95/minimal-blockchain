package apis.domain.responses;

import domain.transaction.Transaction;

public class GetTransactionResponse extends Response{
    public Transaction transaction;

    public GetTransactionResponse(Transaction transaction) {
        this.transaction = transaction;
    }
}
