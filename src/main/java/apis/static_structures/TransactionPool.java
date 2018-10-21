package apis.static_structures;

import db.DBSingletons;
import domain.block.StoredBlock;
import domain.transaction.Transaction;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    public boolean has(byte[] key) {
        return transactionPoolDB.get(key) != null;
    }

    public List<Transaction> getAll() {

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
}
