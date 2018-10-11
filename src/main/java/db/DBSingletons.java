package db;

import node.Config;
import node.Node;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

public class DBSingletons {

    public static final String META_FOLDER = "/meta-local";
    public static final String BLOCK_FOLDER = "/block-local";
    public static final String TRANSACTION_FOLDER = "/transaction-local";
    public static final String POOL_FOLDER = "/pool-local";

    private static DBFactory factory;
    private static DB blockDB = null;
    private static DB metaDB = null;
    private static DB transactionDB = null;
    private static DB poolDB = null;

    public static void init(String dbFolder){
        Options options = new Options();
        options.createIfMissing(true);
        try {
            factory = (DBFactory) Node.class.getClassLoader().loadClass(System.getProperty("leveldb.factory", "org.iq80.leveldb.impl.Iq80DBFactory")).newInstance();
            blockDB = factory.open(new File(dbFolder + BLOCK_FOLDER), options);
            metaDB = factory.open(new File(dbFolder + META_FOLDER), options);
            transactionDB = factory.open(new File(dbFolder + TRANSACTION_FOLDER), options);
            poolDB = factory.open(new File(dbFolder + POOL_FOLDER), options);
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

    public static void restart(String dbFolder) {
        Options options = new Options();
        try {
            factory.destroy(new File(Config.dbFolder+BLOCK_FOLDER), options);
            factory.destroy(new File(Config.dbFolder+META_FOLDER), options);
            factory.destroy(new File(Config.dbFolder+TRANSACTION_FOLDER), options);

            init(dbFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DBFactory getFactoryInstance() {
        return factory;
    }

    public static DB getBlockDB() {
        return blockDB;
    }

    public static DB getMetaDB() {
        return metaDB;
    }

    public static DB getTransactionDB() {
        return transactionDB;
    }
}
