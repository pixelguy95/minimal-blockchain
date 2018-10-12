package apis.static_structures;

import db.DBSingletons;
import domain.block.Block;
import domain.block.StoredBlock;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

public class Blockchain {

    private static Blockchain instance = null;

    public Blockchain(HashMap<ByteBuffer, StoredBlock> chain, HashMap<ByteBuffer, StoredBlock> leafs) {
        this.chain = chain;
        this.leafs = leafs;
    }

    public static Blockchain getInstance() {
        if(instance == null) {
            instance = newInstance();
        }

        return instance;
    }

    public static void destroy() {
        instance = null;
    }

    private HashMap<ByteBuffer, StoredBlock> chain;
    private HashMap<ByteBuffer, StoredBlock> leafs;

    private static Blockchain newInstance() {

        HashMap<ByteBuffer, StoredBlock> chain = new HashMap<>();
        HashMap<ByteBuffer, StoredBlock> leafs = new HashMap<>();

        try {
            DBIterator iterator = DBSingletons.getBlockHeaderDB().iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                StoredBlock sb = (StoredBlock) SerializationUtils.deserialize(iterator.peekNext().getValue());
                chain.put(ByteBuffer.wrap(key), sb);
            }
            iterator.close();

            byte[] leafBytes = DBSingletons.getMetaDB().get("leafkey".getBytes());
            if (leafBytes != null && leafBytes.length != 0) {
                leafs = (HashMap<ByteBuffer, StoredBlock>) SerializationUtils.deserialize(DBSingletons.getMetaDB().get("leafkey".getBytes()));
            } else {
                leafs = new HashMap<>();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Blockchain(chain, leafs);
    }

    public void addBlock(Block block) throws Exception {
        byte[] newHash = block.header.getHash();
        if (leafs.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            // new block, add after one of the leaves
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(leafs.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            leafs.remove(ByteBuffer.wrap(block.header.prevBlockHash));
            DBSingletons.getBlockDB().put(newHash, SerializationUtils.serialize(block));

        } else if (chain.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            // contested block found.
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(chain.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            DBSingletons.getBlockDB().put(newHash, SerializationUtils.serialize(block));

        } else if (chain.size() == 0) {
            // genesis block
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(0, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            DBSingletons.getBlockDB().put(newHash, SerializationUtils.serialize(block));

        } else {
            throw new Exception("No such prev block");
        }
    }

    public HashMap<ByteBuffer, StoredBlock> getChain() {
        return chain;
    }

    public HashMap<ByteBuffer, StoredBlock> getLeafs() {
        return leafs;
    }

    public void persistLocalVariables() {
        DBSingletons.getMetaDB().put("leafkey".getBytes(), SerializationUtils.serialize(leafs));
        chain.entrySet().stream().forEach(entry -> {
            DBSingletons.getBlockHeaderDB().put(entry.getKey().array(), SerializationUtils.serialize(entry.getValue()));
        });
    }
}
