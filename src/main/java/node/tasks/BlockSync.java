package node.tasks;

import apis.domain.Host;
import apis.domain.responses.GetAllBlockHashesResponse;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import apis.utils.*;
import apis.utils.validators.BlockValidator;
import apis.utils.validators.TransactionValidator;
import apis.utils.validators.Validator;
import apis.utils.wrappers.BlockRESTWrapper;
import domain.block.Block;
import node.Config;
import org.apache.commons.collections4.ListUtils;
import org.restlet.resource.ResourceException;

import java.nio.ByteBuffer;
import java.util.*;

public class BlockSync {

    private KnownNodesList knownNodesList;
    private Blockchain blockchain;
    private UTXO utxo;
    private TransactionPool transactionPool;

    private Config config;

    private BlockValidator blockValidator;
    private TransactionValidator transactionValidator;


    public BlockSync(KnownNodesList knownNodesList,
                     Blockchain blockchain,
                     UTXO utxo,
                     TransactionPool transactionPool,
                     Config config,
                     BlockValidator blockValidator,
                     TransactionValidator transactionValidator) {
        this.knownNodesList = knownNodesList;
        this.blockchain = blockchain;
        this.utxo = utxo;
        this.transactionPool = transactionPool;
        this.config = config;
        this.blockValidator = blockValidator;
        this.transactionValidator = transactionValidator;
    }

    /**
     * Below text is a bit old now
     *
     * WARNING!
     * This will work given that the block chain is small.
     * If there are 100.000 blocks all of them 1MB you will roughly have to load 100GB into memory.
     * This will not be possible
     *
     * What we need is some way to fetch only the next block in the chain and add it before getting the next.
     * This can not be derived from hashes alone though.
     *
     */
    public boolean sync() {
        List<String> partitionedHashes = getHashSetOfAllBlocks(blockchain.getBestHeight() - 1);
        for(String hash : partitionedHashes) {
            List<Block> blocksInThisPartition = getBlocksFromDifferentNodes(Arrays.asList(hash));

            for(Block b : blocksInThisPartition) {
                if(!blockchain.getChain().containsKey(b.header.getHash())) {

                    if(config.validateNewBlocks) {
                        Validator.Result result = blockValidator.validate(b);
                        if(result.passed) {
                            BlockAddingManager.addBlockAndManageUTXOs(blockchain, utxo, transactionPool, transactionValidator, config, b);
                        } else {
                            System.err.println("Block sync failed: " + result.resaon);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private List<String> getHashSetOfAllBlocks(long height) {
        List<String> ret = new ArrayList<>();
        for(Host h : knownNodesList.getKnownNodes()) {
            try {
                GetAllBlockHashesResponse response = BlockRESTWrapper.getAllBlockHashesFromHeight(h, (int)height);

                for(String hash : response.hashes) {
                    if(!ret.contains(hash)){
                        ret.add(hash);
                    }
                }

            } catch (ResourceException e) {

            }
        }

        return ret;
    }

    private List<Block> getBlocksFromDifferentNodes(List<String> blockHashes) {
        List<Block> blockBuffer = new ArrayList<>();
        for(String hash : blockHashes) {
            byte[] byteHash = Base64.getUrlDecoder().decode(hash);
            if(!blockchain.getChain().containsKey(ByteBuffer.wrap(byteHash))) {
                Block b = null;
                while(b == null) {

                    Host attempt = randomNode();
                    try {
                        b = BlockRESTWrapper.getBlock(attempt, byteHash).block;
                        blockBuffer.add(b);
                    } catch (ResourceException e) {
                        //Host/Node was not responding.
                        knownNodesList.removeNode(attempt);
                    }
                }
            }
        }

        return blockBuffer;
    }

    private Host randomNode() {
        List<Host> all = new ArrayList<>(knownNodesList.getKnownNodes());
        Random r = new Random();
        return all.get(r.nextInt(all.size()));
    }
}
