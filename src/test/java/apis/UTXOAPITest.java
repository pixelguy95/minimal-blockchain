package apis;

import apis.domain.Host;
import apis.domain.responses.GetOutputByAddressResponse;
import apis.domain.responses.GetOutputResponse;
import apis.utils.wrappers.BlockRESTWrapper;
import apis.utils.wrappers.UTXORESTWrapper;
import domain.block.Block;
import domain.utxo.UTXOIdentifier;
import node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;

import java.security.PublicKey;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UTXOAPITest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1", "-nm"};
    public Node node;
    public Host localHost = new Host("localhost:13337");
    private PublicKey pub;

    @Before
    public void setUp() throws Exception {
        pub = ECKeyManager.generateNewKeyPair().getPublic();

        node = new Node(initialNodeArgs);
        node.config.validateNewBlocks = false;
        Thread.sleep(100);
    }

    @After
    public void tearDown() throws Exception {
        node.kill();
        node.destroyPersistantData();
    }

    @Test
    public void fetchUTXO() {
        node.config.safeBlockLength = 4;
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pub);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pub);
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pub);
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pub);

        BlockRESTWrapper.newBlock(localHost, block1);
        BlockRESTWrapper.newBlock(localHost, block2);
        BlockRESTWrapper.newBlock(localHost, block3);
        BlockRESTWrapper.newBlock(localHost, block4);

        GetOutputResponse o = UTXORESTWrapper.getUTXO(localHost, new UTXOIdentifier(genesis.coinbase.fullHash(), 0));
        assertFalse(o.error);
        assertEquals(genesis.coinbase.outputs.get(0), o.output);
    }

    @Test
    public void fetchUTXOByAddress() {
        node.config.safeBlockLength = 4;
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pub);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pub);
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pub);
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pub);
        Block block5 = new Block(Arrays.asList(), block4.header.getHash(), pub);

        BlockRESTWrapper.newBlock(localHost, block1);
        BlockRESTWrapper.newBlock(localHost, block2);
        BlockRESTWrapper.newBlock(localHost, block3);
        BlockRESTWrapper.newBlock(localHost, block4);
        BlockRESTWrapper.newBlock(localHost, block5);

        GetOutputByAddressResponse r = UTXORESTWrapper.getUTXOByPubKey(localHost, pub);
        System.out.println(node.utxo.getAll().size());
        System.out.println(r.outputs.size());

        assertTrue(r.outputs.size() == 1);
        assertTrue(node.utxo.getAllByPublicKey(pub).size() == 1);
    }
}