package db;

import node.Node;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

public class DBSingletons {

    private static DBFactory factory;
    private static DB blockDB = null;
    private static DB metaDB = null;
    private static DB transactionDB = null;

    public static void init(String dbFolder){
        Options options = new Options();
        options.createIfMissing(true);
        try {
            factory = (DBFactory) Node.class.getClassLoader().loadClass(System.getProperty("leveldb.factory", "org.iq80.leveldb.impl.Iq80DBFactory")).newInstance();
            blockDB = factory.open(new File(dbFolder + "/block-local"), options);
            metaDB = factory.open(new File(dbFolder + "/meta-local"), options);
            transactionDB = factory.open(new File(dbFolder + "/transaction-local"), options);
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
