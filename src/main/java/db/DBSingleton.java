package db;

import cores.Node;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

public class DBSingleton {

    private static DBFactory factory;
    private static DB db = null;

    static {
        Options options = new Options();
        options.createIfMissing(true);
        try {
            factory = (DBFactory) Node.class.getClassLoader().loadClass(System.getProperty("leveldb.factory", "org.iq80.leveldb.impl.Iq80DBFactory")).newInstance();
            db = factory.open(new File("node-local"), options);
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

    public static DB getInstance() {
        return db;
    }
}
