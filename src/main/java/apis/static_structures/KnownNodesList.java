package apis.static_structures;

import apis.domain.Host;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;

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
                knownNodesList = (HashSet<Host>) SerializationUtils.deserialize(serializedList);
            } else {
                knownNodesList = new HashSet<>();
            }
        } finally {

        }
    }

    public synchronized HashSet<Host> getKnownNodes() {
        return knownNodesList;
    }

    public synchronized void addNode(Host h) {
        try {
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
