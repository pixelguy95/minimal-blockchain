package apis.static_structures;

import db.DBSingletons;
import domain.transaction.Transaction;
import org.iq80.leveldb.DB;

/**
 * Right now this acts as a wrapper around the transaction pool db,
 * might implement additional methods later.
 */
public class TransactionPool {

    private static TransactionPool instance;

    public static TransactionPool getInstance() {
        if(instance == null) {
            instance = new TransactionPool();
        }

        return instance;
    }

    private DB transactionPoolDB;

    private TransactionPool() {
        transactionPoolDB = DBSingletons.getPoolDB();
    }

    public Transaction get(byte[] key) {
        return Transaction.fromBytes(transactionPoolDB.get(key));
    }

    public void put(Transaction t) {
        transactionPoolDB.put(t.fullHash(), t.serialize());
    }
}
