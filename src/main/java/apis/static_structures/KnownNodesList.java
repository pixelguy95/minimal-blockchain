package apis.static_structures;

import apis.domain.Host;
import db.DBSingletons;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;

public class KnownNodesList {

    private final byte[] KEY = "hosts".getBytes();
    private DB metaDB;
    private HashSet<Host> knownNodesList;

    public KnownNodesList(DB metaDB) {
        this.metaDB = metaDB;
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

    public HashSet<Host> getKnownNodes() {
        return knownNodesList;
    }

    public synchronized void addNode(Host h) {
        try {
            if(metaDB == null) {
                System.out.println("FAILED");
            } else {

            }

            knownNodesList.add(h);
            metaDB.put(KEY, SerializationUtils.serialize(knownNodesList));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void removeNode(Host h) {
        knownNodesList.remove(h);
        metaDB.put(KEY, SerializationUtils.serialize(knownNodesList));
    }

    public synchronized void removeAllNodes(HashSet<Host> all) {
        knownNodesList.removeAll(all);
        metaDB.put(KEY, SerializationUtils.serialize(knownNodesList));
    }

    public synchronized List<Host> getAllNodesUnderIP(String ip) {
        List<Host> ret = new ArrayList<>();
        knownNodesList.stream().forEach(node->{
            if(node.ip.equals(ip)) {
                ret.add(node);
            }
        });

        return ret;
    }
}
