package node.domain;

import db.DBSingletons;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KnownNodesList {

    private static final byte[] KEY = "hosts".getBytes();
    private static DB metaDB;
    private static List<Host> knownNodesList;

    static {
        metaDB = DBSingletons.getMetaDB();
        try {
            byte[] serializedList = metaDB.get(KEY);

            if(serializedList != null && serializedList.length != 0) {
                System.out.println("loaded nodes list");
                knownNodesList = (List<Host>) SerializationUtils.deserialize(serializedList);
                getKnownNodes().stream().forEach(n-> System.out.println(n.ip + ":" + n.port));
            } else {
                System.out.println("new empty nodes list");
                knownNodesList = new ArrayList<>();
            }

        } finally {

        }
    }

    public static List<Host> getKnownNodes() {
        return knownNodesList;
    }

    public static void addNode(Host h) {
        knownNodesList.add(h);
        metaDB.put(KEY, SerializationUtils.serialize((Serializable) knownNodesList));
    }

    public static class Host implements Serializable {
        public String ip;
        public int port;

        public Host(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }
}
