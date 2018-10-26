package db;

import node.Config;
import node.Node;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

public class DBHolder {

    public static final String META_FOLDER = "/meta-local";
    public static final String BLOCK_FOLDER = "/block-local";
    public static final String BLOCKHEADER_FOLDER = "/blockheader-local";
    public static final String LEAF_FOLDER = "/leafs-local";
    public static final String TRANSACTION_FOLDER = "/transaction-local";
    public static final String POOL_FOLDER = "/pool-local";
    public static final String UTXO_FOLDER = "/utxo-local";

    private DBFactory factory;
    private DB blockDB = null;
    private DB blockHeaderDB = null;
    private DB leafDB = null;
    private DB metaDB = null;
    private DB transactionDB = null;
    private DB poolDB = null;
    private DB utxoDB = null;

    public DBHolder(String dbFolder){
        Options options = new Options();
        options.createIfMissing(true);
        try {
            factory = (DBFactory) Node.class.getClassLoader().loadClass(System.getProperty("leveldb.factory", "org.iq80.leveldb.impl.Iq80DBFactory")).newInstance();
            blockDB = factory.open(new File(dbFolder + BLOCK_FOLDER), options);
            blockHeaderDB = factory.open(new File(dbFolder + BLOCKHEADER_FOLDER), options);
            leafDB = factory.open(new File(dbFolder + LEAF_FOLDER), options);
            metaDB = factory.open(new File(dbFolder + META_FOLDER), options);
            transactionDB = factory.open(new File(dbFolder + TRANSACTION_FOLDER), options);
            poolDB = factory.open(new File(dbFolder + POOL_FOLDER), options);
            utxoDB = factory.open(new File(dbFolder + UTXO_FOLDER), options);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void destroy(String dbFolder) {
        Options options = new Options();
        try {
            blockDB.close();
            leafDB.close();
            blockHeaderDB.close();
            metaDB.close();
            transactionDB.close();
            poolDB.close();
            utxoDB.close();

            factory.destroy(new File(dbFolder+BLOCK_FOLDER), options);
            factory.destroy(new File(dbFolder+BLOCKHEADER_FOLDER), options);
            factory.destroy(new File(dbFolder+LEAF_FOLDER), options);
            factory.destroy(new File(dbFolder+POOL_FOLDER), options);
            factory.destroy(new File(dbFolder+META_FOLDER), options);
            factory.destroy(new File(dbFolder+TRANSACTION_FOLDER), options);
            factory.destroy(new File(dbFolder+UTXO_FOLDER), options);

            //init(dbFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DBFactory getFactory() {
        return factory;
    }

    public DB getBlockDB() {
        return blockDB;
    }

    public DB getMetaDB() {
        return metaDB;
    }

    public DB getTransactionDB() {
        return transactionDB;
    }

    public DB getBlockHeaderDB() {
        return blockHeaderDB;
    }

    public DB getLeafDB() {
        return leafDB;
    }

    public DB getPoolDB() {
        return poolDB;
    }

    public DB getUtxoDB() {
        return utxoDB;
    }

    public void restart(String dbFolder) {
        Options options = new Options();
        options.createIfMissing(true);
        try {
            factory = (DBFactory) Node.class.getClassLoader().loadClass(System.getProperty("leveldb.factory", "org.iq80.leveldb.impl.Iq80DBFactory")).newInstance();
            blockDB = factory.open(new File(dbFolder + BLOCK_FOLDER), options);
            blockHeaderDB = factory.open(new File(dbFolder + BLOCKHEADER_FOLDER), options);
            leafDB = factory.open(new File(dbFolder + LEAF_FOLDER), options);
            metaDB = factory.open(new File(dbFolder + META_FOLDER), options);
            transactionDB = factory.open(new File(dbFolder + TRANSACTION_FOLDER), options);
            poolDB = factory.open(new File(dbFolder + POOL_FOLDER), options);
            utxoDB = factory.open(new File(dbFolder + UTXO_FOLDER), options);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
