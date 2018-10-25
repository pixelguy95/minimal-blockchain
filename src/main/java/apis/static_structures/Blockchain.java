package apis.static_structures;

import domain.block.Block;
import domain.block.StoredBlock;
import domain.transaction.Output;
import domain.utxo.UTXOIdentifier;
import node.Config;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.math.IntRange;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.IntStream;

public class Blockchain {

    private HashMap<ByteBuffer, StoredBlock> chain;
    private HashMap<ByteBuffer, StoredBlock> leafs;
    private HashMap<ByteBuffer, StoredBlock> orphans;
    private DB blockDB;
    private DB metaDB;
    private DB blockHeaderDB;
    private Config config;

    public Blockchain(DB blockDB, DB blockHeaderDB, DB metaDB, Config config) {

        this.blockDB = blockDB;
        this.blockHeaderDB = blockHeaderDB;
        this.metaDB = metaDB;
        this.config = config;

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
        this.orphans = new HashMap<>();
    }

    public synchronized void addBlock(Block block) {
        byte[] newHash = block.header.getHash();
        if (leafs.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            // new block, add after one of the leaves
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(leafs.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            leafs.remove(ByteBuffer.wrap(block.header.prevBlockHash));
            blockDB.put(newHash, SerializationUtils.serialize(block));
            checkOrphans();

        } else if (chain.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            // contested block found.
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(chain.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            blockDB.put(newHash, SerializationUtils.serialize(block));
            checkOrphans();

        } else if (chain.size() == 0) {
            // genesis block
            chain.put(ByteBuffer.wrap(newHash), new StoredBlock(0, block.header));
            leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
            blockDB.put(newHash, SerializationUtils.serialize(block));

        } else if(config.allowOrphanBlocks){
            // orphan block
            orphans.put(ByteBuffer.wrap(newHash), new StoredBlock(-1, block.header));
        } else {
            throw new RuntimeException("No such prev block, orphans not allowed");
        }
    }

    private void checkOrphans() {
        if(orphans.size() <= 0)
            return;

        while(orphans.size() > 0) {

            boolean keepGoing = false;
            for(StoredBlock block : orphans.values()) {
                if(orphans.containsKey(block.blockHeader.prevBlockHash))
                    continue;

                byte[] newHash = block.blockHeader.getHash();
                if (leafs.containsKey(ByteBuffer.wrap(block.blockHeader.prevBlockHash))) {
                    // new block, add after one of the leaves
                    chain.put(ByteBuffer.wrap(newHash), new StoredBlock(leafs.get(ByteBuffer.wrap(block.blockHeader.prevBlockHash)).height + 1, block.blockHeader));
                    leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
                    leafs.remove(ByteBuffer.wrap(block.blockHeader.prevBlockHash));
                    blockDB.put(newHash, SerializationUtils.serialize(block));

                    orphans.remove(ByteBuffer.wrap(newHash));
                    keepGoing = true;
                    break;

                } else if (chain.containsKey(ByteBuffer.wrap(block.blockHeader.prevBlockHash))) {
                    // contested block found.
                    chain.put(ByteBuffer.wrap(newHash), new StoredBlock(chain.get(ByteBuffer.wrap(block.blockHeader.prevBlockHash)).height + 1, block.blockHeader));
                    leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
                    blockDB.put(newHash, SerializationUtils.serialize(block));

                    orphans.remove(ByteBuffer.wrap(newHash));
                    keepGoing = true;
                    break;
                }
            }

            if(keepGoing)
                continue;

            break;
        }
    }

    public Map<UTXOIdentifier, Output> getUTXOCandidates() {
        Map<UTXOIdentifier, Output> newUtxos = new HashMap<>();

        Block temp = getTopBlock();
        for(int i = 0; i < config.safeBlockLength; i++) {
            if(!chain.containsKey(ByteBuffer.wrap(temp.header.getHash()))) {
                return newUtxos;
            }

            temp = (Block) SerializationUtils.deserialize(blockDB.get(temp.header.prevBlockHash));
        }

        IntStream.range(0, temp.transactions.size()).forEach(i -> {

        });


        //TODO: Think about this properly, wont work as output/inputs is now

        return null;
    }

    public Block getTopBlock() {

        if(leafs.size()==1) {
            return (Block) SerializationUtils.deserialize(blockDB.get(leafs.values().iterator().next().blockHeader.getHash()));
        }
        StoredBlock heighestBlock = null;
        int largestBlockHeight = -1;

        Collection<StoredBlock> allLeafs = leafs.values();
        for(StoredBlock block : allLeafs) {
            if(block.height > largestBlockHeight) {
                heighestBlock = (StoredBlock) SerializationUtils.deserialize(blockDB.get(block.blockHeader.getHash()));
            }
        }

        return (Block) SerializationUtils.deserialize(blockDB.get(heighestBlock.blockHeader.getHash()));
    }

    public synchronized long getBestHeight() {

        if(leafs.size() == 0) {
            return 0;
        }

        return leafs.values().stream().mapToLong(l->l.height).max().getAsLong() + 1;
    }

    public synchronized HashMap<ByteBuffer, StoredBlock> getChain() {
        return chain;
    }

    public synchronized HashMap<ByteBuffer, StoredBlock> getLeafs() {
        return leafs;
    }

    public synchronized Block getBlock(byte[] hash) {
        return (Block) SerializationUtils.deserialize(blockDB.get(hash));
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
