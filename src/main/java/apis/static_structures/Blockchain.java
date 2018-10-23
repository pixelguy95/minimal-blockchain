package apis.static_structures;

import db.DBSingletons;
import domain.block.Block;
import domain.block.StoredBlock;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

public class Blockchain {

    private HashMap<ByteBuffer, StoredBlock> chain;
    private HashMap<ByteBuffer, StoredBlock> leafs;
    private DB blockDB;
    private DB metaDB;
    private DB blockHeaderDB;

    public Blockchain(DB blockDB, DB blockHeaderDB, DB metaDB) {

        this.blockDB = blockDB;
        this.blockHeaderDB = blockHeaderDB;
        this.metaDB = metaDB;

        HashMap<ByteBuffer, StoredBlock> chain = new HashMap<>();
        HashMap<ByteBuffer, StoredBlock> leafs = new HashMap<>();

        try {
            DBIterator iterator = blockHeaderDB.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                StoredBlock sb = (StoredBlock) SerializationUtils.deserialize(iterator.peekNext().getValue());
                chain.put(ByteBuffer.wrap(key), sb);
            }
            iterator.close();

            byte[] leafBytes = metaDB.get("leafkey".getBytes());
            if (leafBytes != null && leafBytes.length != 0) {
                leafs = (HashMap<ByteBuffer, StoredBlock>) SerializationUtils.deserialize(metaDB.get("leafkey".getBytes()));
            } else {
                leafs = new HashMap<>();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        this.chain = chain;
        this.leafs = leafs;
    }

    public synchronized void addBlock(Block block) throws Exception {
        byte[] newHash = block.header.getHash();
        if (leafs.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            // new block, add after one of the leaves
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(leafs.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            leafs.remove(ByteBuffer.wrap(block.header.prevBlockHash));
            blockDB.put(newHash, SerializationUtils.serialize(block));

        } else if (chain.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            // contested block found.
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(chain.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            blockDB.put(newHash, SerializationUtils.serialize(block));

        } else if (chain.size() == 0) {
            // genesis block
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(0, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            blockDB.put(newHash, SerializationUtils.serialize(block));

        } else {
            throw new Exception("No such prev block");
        }
    }

    public synchronized long getBestHeight() {

        if(leafs.size() == 0) {
            return 0;
        }

        return leafs.values().stream().mapToLong(l->l.height).max().getAsLong();
    }

    public synchronized HashMap<ByteBuffer, StoredBlock> getChain() {
        return chain;
    }

    public synchronized HashMap<ByteBuffer, StoredBlock> getLeafs() {
        return leafs;
    }

    /**
     * This might be really stupid and useless, will probably be removed
     */
    public synchronized void persistLocalVariables() {
        metaDB.put("leafkey".getBytes(), SerializationUtils.serialize(leafs));
        chain.entrySet().stream().forEach(entry -> {
            blockHeaderDB.put(entry.getKey().array(), SerializationUtils.serialize(entry.getValue()));
        });
    }
}
