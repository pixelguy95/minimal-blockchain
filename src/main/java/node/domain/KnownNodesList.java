package node.domain;

import db.DBSingletons;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
                getKnownNodes().stream().forEach(n-> System.out.println(n.ip + ":" + n.port));
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

    public static class Host implements Serializable {
        public String ip;
        public int port;

        public Host(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public Host(String ipAndPort) {
            this.ip = ipAndPort.split(":")[0];
            this.port = Integer.parseInt(ipAndPort.split(":")[1]);
        }
    }
}
