package node.tasks;

import apis.domain.Host;
import apis.domain.responses.GetAllBlockHashesResponse;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.utils.BlockRESTWrapper;
import domain.block.Block;
import org.restlet.resource.ResourceException;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockSync extends AbstractTask {

    private KnownNodesList knownNodesList;
    private Blockchain blockchain;

    public BlockSync(AtomicBoolean keepAlive, KnownNodesList knownNodesList, Blockchain blockchain) {
        super(keepAlive);
        this.knownNodesList = knownNodesList;
        this.blockchain = blockchain;
    }

    /**
     * WARNING!
     * This will work given that the block chain is small.
     * If there are 100.000 blocks all of them 1MB you will roughly have to load 100GB into memory.
     * This will not be possible
     *
     * What we need is some way to fetch only the next block in the chain and add it before getting the next.
     * This can not be derived from hashes alone though.
     *
     */
    @Override
    public void run() {
        HashSet<String> blockHashes = getHashSetOfAllBlocks();
        List<Block> blockBuffer = getBlocksFromDifferentNodes(blockHashes);

        blockBuffer.stream().forEach(b->blockchain.addBlock(b));
    }

    private HashSet<String> getHashSetOfAllBlocks() {
        HashSet<String> ret = new HashSet<>();
        for(Host h : knownNodesList.getKnownNodes()) {
            try {
                GetAllBlockHashesResponse response = BlockRESTWrapper.getAllBlockHashes(h);
                ret.addAll(response.hashes);
            } catch (ResourceException e) {

            }
        }

        return ret;
    }

    private List<Block> getBlocksFromDifferentNodes(HashSet<String> blockHashes) {
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
