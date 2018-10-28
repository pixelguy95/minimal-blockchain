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

    @Override
    public void run() {

        HashSet<String> blockHashes = new HashSet<>();
        for(Host h : knownNodesList.getKnownNodes()) {
            try {
                GetAllBlockHashesResponse response = BlockRESTWrapper.getAllBlockHashes(h);
                blockHashes.addAll(response.hashes);
            } catch (ResourceException e) {

            }
        }

        for(String hash : blockHashes) {
            byte[] byteHash = Base64.getUrlDecoder().decode(hash);
            if(!blockchain.getChain().containsKey(ByteBuffer.wrap(byteHash))) {
                Block b = null;
                while(b == null) {
                    try {
                        b = BlockRESTWrapper.getBlock(randomNode(), byteHash).block;
                        blockchain.addBlock(b);
                    } catch (ResourceException e) {

                    }
                }
            }
        }
    }

    private Host randomNode() {
        List<Host> all = new ArrayList<>(knownNodesList.getKnownNodes());
        Random r = new Random();
        return all.get(r.nextInt(all.size()));
    }
}
