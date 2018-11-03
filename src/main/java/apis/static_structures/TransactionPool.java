package apis.static_structures;

import domain.transaction.Transaction;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Right now this acts as a wrapper around the transaction pool db,
 * might implement additional methods later.
 */
public class TransactionPool {

    private DB transactionPoolDB;

    public TransactionPool(DB transactionPoolDB) {
        this.transactionPoolDB = transactionPoolDB;
    }

    public synchronized Transaction get(byte[] key) {
        return Transaction.fromBytes(transactionPoolDB.get(key));
    }

    public synchronized void put(Transaction t) {
        transactionPoolDB.put(t.fullHash(), t.serialize());
    }

    public synchronized void remove(byte[] txid) {
        transactionPoolDB.delete(txid);
    }

    public synchronized boolean has(byte[] key) {
        return transactionPoolDB.get(key) != null;
    }

    public synchronized List<Transaction> getAll() {

        List<Transaction> all = new ArrayList<>();
        DBIterator iterator = transactionPoolDB.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            all.add(Transaction.fromBytes(iterator.peekNext().getValue()));
        }

        try {
            iterator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return all;
    }

    public synchronized Transaction getOneTransaction() {
        DBIterator iterator = transactionPoolDB.iterator();
        iterator.seekToFirst();
        return Transaction.fromBytes(iterator.peekNext().getValue());
    }

    public synchronized List<Transaction> getNTransactions(int n) {
        DBIterator iterator = transactionPoolDB.iterator();
        iterator.seekToFirst();

        List<Transaction> nTransactions = new ArrayList<>();
        iterator.forEachRemaining(entry -> nTransactions.add(Transaction.fromBytes(entry.getValue())));

        return nTransactions;
    }
}
