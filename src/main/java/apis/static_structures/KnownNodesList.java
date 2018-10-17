package apis.static_structures;

import apis.domain.Host;
import db.DBSingletons;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class KnownNodesList {

    private static final byte[] KEY = "hosts".getBytes();
    private static DB metaDB;
    private static HashSet<Host> knownNodesList;

    static {
        metaDB = DBSingletons.getMetaDB();
        try {
            byte[] serializedList = metaDB.get(KEY);

            if(serializedList != null && serializedList.length != 0) {
                System.out.println("loaded nodes list");
                knownNodesList = (HashSet<Host>) SerializationUtils.deserialize(serializedList);
                //getKnownNodes().stream().forEach(n-> System.out.println(n.ip + ":" + n.port));
            } else {
                System.out.println("new empty nodes list");
                knownNodesList = new HashSet<>();
            }
        } finally {

        }
    }

    public static HashSet<Host> getKnownNodes() {
        return knownNodesList;
    }

    public static void addNode(Host h) {
        knownNodesList.add(h);
        metaDB.put(KEY, SerializationUtils.serialize(knownNodesList));
    }

    public static void removeNode(Host h) {
        knownNodesList.remove(h);
        metaDB.put(KEY, SerializationUtils.serialize(knownNodesList));
    }

    public static void removeAllNodes(HashSet<Host> all) {
        knownNodesList.removeAll(all);
        metaDB.put(KEY, SerializationUtils.serialize(knownNodesList));
    }
}
