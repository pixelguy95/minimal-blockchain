package apis.static_structures;

import domain.block.Block;
import domain.block.StoredBlock;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.utxo.UTXOIdentifier;
import node.Config;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Blockchain {

    private static byte[] genesisBlockHash = null;

    private HashMap<ByteBuffer, StoredBlock> chain;
    private HashMap<ByteBuffer, StoredBlock> leafs;
    private HashMap<ByteBuffer, Block> orphans;
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
                leafs = deserilizeLeafs(leafBytes);
            } else {
                leafs = new HashMap<>();
            }

            genesisBlockHash = metaDB.get("genesisblockhash".getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.chain = chain;
        this.leafs = leafs;
        this.orphans = new HashMap<>();

        if(chain.size() == 0) {
            addBlock(Block.generateGenesisBlock(this));
        }
    }

    /**
     * This constructor is for testing purposes only. Here you have the chance to provide your own genesisblock
     * @param blockDB
     * @param blockHeaderDB
     * @param metaDB
     * @param config
     * @param customGenesisBlock
     */
    public Blockchain(DB blockDB, DB blockHeaderDB, DB metaDB, Config config, Block customGenesisBlock) {

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
                leafs = deserilizeLeafs(leafBytes);
            } else {
                leafs = new HashMap<>();
            }

            genesisBlockHash = metaDB.get("genesisblockhash".getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.chain = chain;
        this.leafs = leafs;
        this.orphans = new HashMap<>();

        if(chain.size() == 0) {
            addBlock(customGenesisBlock);
        }
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
            genesisBlockHash = newHash;
            metaDB.put("genesisblockhash".getBytes(), newHash);

        } else if(config.allowOrphanBlocks){
            // orphan block
            orphans.put(ByteBuffer.wrap(newHash), block);
        } else {
            throw new RuntimeException("No such prev block, orphans not allowed");
        }

        persistLocalVariables();
    }

    private void checkOrphans() {
        if(orphans.size() <= 0)
            return;

        while(orphans.size() > 0) {

            boolean keepGoing = false;
            for(Block block : orphans.values()) {
                if(orphans.containsKey(block.header.prevBlockHash))
                    continue;

                byte[] newHash = block.header.getHash();
                if (leafs.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
                    // new block, add after one of the leaves
                    chain.put(ByteBuffer.wrap(newHash), new StoredBlock(leafs.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
                    leafs.put(ByteBuffer.wrap(newHash), chain.get(ByteBuffer.wrap(newHash)));
                    leafs.remove(ByteBuffer.wrap(block.header.prevBlockHash));
                    blockDB.put(newHash, SerializationUtils.serialize(block));

                    orphans.remove(ByteBuffer.wrap(newHash));
                    keepGoing = true;
                    break;

                } else if (chain.containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
                    // contested block found.
                    chain.put(ByteBuffer.wrap(newHash), new StoredBlock(chain.get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1, block.header));
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

    public Block walkSafeBLockLength() {
        //Go safeBlockLength blocks down the chain
        Block temp = getTopBlock();
        for(int i = 0; i < config.safeBlockLength; i++) {
            if(!chain.containsKey(ByteBuffer.wrap(temp.header.prevBlockHash))) {
                return null;
            }

            temp = (Block) SerializationUtils.deserialize(blockDB.get(temp.header.prevBlockHash));
        }

        return temp;
    }

    public Map<UTXOIdentifier, Output> getUTXOCandidates() {
        Map<UTXOIdentifier, Output> newUtxos = new HashMap<>();

        Block temp = walkSafeBLockLength();

        if(temp == null) {
            return newUtxos;
        }

        //Add all outputs of that block into the hashmap
        for(Transaction t : temp.transactions) {
            AtomicInteger index = new AtomicInteger(0);
            List<UTXOIdentifier> ids = t.outputs.stream()
                    .map(o-> new UTXOIdentifier(t.fullHash(), index.getAndIncrement()))
                    .collect(Collectors.toList());

            for(int i = 0; i < ids.size(); i++) {
                newUtxos.put(ids.get(i), t.outputs.get(i));
            }
        }

        //The coinbase transaction also.
        newUtxos.put(new UTXOIdentifier(temp.coinbase.fullHash(), 0), temp.coinbase.outputs.get(0));
        return newUtxos;
    }

    public Map<UTXOIdentifier, Output> getUTXORemovalCandidates() {
        Map<UTXOIdentifier, Output> oldUTXOS = new HashMap<>();

        Block temp = walkSafeBLockLength();

        if(temp == null) {
            return oldUTXOS;
        }

        //Add all reference outputs from the inputs into the hashmap
        for(Transaction t : temp.transactions) {
            AtomicInteger index = new AtomicInteger(0);
            List<UTXOIdentifier> ids = t.inputs.stream()
                    .map(input-> new UTXOIdentifier(input.transactionHash, input.outputIndex))
                    .collect(Collectors.toList());

            for(int i = 0; i < ids.size(); i++) {
                oldUTXOS.put(ids.get(i), t.outputs.get(i));
            }
        }

        return oldUTXOS;
    }

    /**
     * Removes all leafs that are safeBlockDistance away from the top block height
     * Only removed from the leaf list. Not from the blockdata (ATM)
     * @return list of all blocks removed
     */
    public List<StoredBlock> prune() {
        List<StoredBlock> removals = new ArrayList<>();

        leafs.values().stream().forEach(leaf->{
            if(leaf.height + config.safeBlockLength < getBestHeight()) {
                removals.add(leaf);
            }
        });

        removals.stream().forEach(r->leafs.remove(ByteBuffer.wrap(r.blockHeader.getHash())));

        return removals;
    }

    public Transaction getTransactionByTXID(byte[] txid) {
        Block b = getTopBlock();
        while(b != null) {

            for(Transaction t : b.transactions) {
                if(ByteBuffer.wrap(t.fullHash()).equals(ByteBuffer.wrap(txid))) {
                    return t;
                }
            }

            if(ByteBuffer.wrap(b.coinbase.fullHash()).equals(ByteBuffer.wrap(txid))) {
                return b.coinbase;
            }

            if(!chain.containsKey(ByteBuffer.wrap(b.header.prevBlockHash))) {
                b = null;
                continue;
            }

            b = (Block) SerializationUtils.deserialize(blockDB.get(b.header.prevBlockHash));
        }

        return null;
    }

    /**
     * Returns the leaf with highest height
     *
     * This method is a bit of a problem, it will return only one even though there can be more than one top block.
     *
     * @return
     */
    public Block getTopBlock() {

        if(leafs.size()==1) {
            return (Block) SerializationUtils.deserialize(blockDB.get(leafs.values().iterator().next().blockHeader.getHash()));
        }
        StoredBlock heighestBlock = null;
        int largestBlockHeight = -1;

        Collection<StoredBlock> allLeafs = leafs.values();
        for(StoredBlock block : allLeafs) {
            if(block.height > largestBlockHeight) {
                heighestBlock = chain.get(ByteBuffer.wrap(block.blockHeader.getHash()));
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

    public synchronized Block getGenesisBlock() {
        return (Block) SerializationUtils.deserialize(blockDB.get(genesisBlockHash));
    }

    private HashMap<ByteBuffer, StoredBlock> deserilizeLeafs(byte[] leafBytes) {
        List<StoredBlock> asList = (ArrayList<StoredBlock>) SerializationUtils.deserialize(leafBytes);
        HashMap<ByteBuffer, StoredBlock> ret = new HashMap<>();
        asList.stream().forEach(b->ret.put(ByteBuffer.wrap(b.hashOfThisBlock()), b));
        return ret;
    }

    public synchronized void persistLocalVariables() {
        metaDB.put("leafkey".getBytes(), SerializationUtils.serialize(new ArrayList<>(leafs.values())));
        chain.entrySet().stream().forEach(entry -> {
            blockHeaderDB.put(entry.getKey().array(), SerializationUtils.serialize(entry.getValue()));
        });
    }
}
