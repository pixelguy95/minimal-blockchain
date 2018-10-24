package apis;

import apis.domain.Host;
import apis.domain.requests.NewBlockFoundRequest;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.BlockHeightResponse;
import apis.domain.responses.BooleanResponse;
import apis.domain.responses.GetBlockResponse;
import apis.domain.responses.NewTransactionResponse;
import apis.utils.BlockRESTWrapper;
import domain.block.Block;
import node.Node;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utils.RESTUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.Assert.*;

public class BlockAPITest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1"};
    public Node node;

    public Host localHost = new Host("localhost:13337");

    @Before
    public void setUp() throws Exception {
        node = new Node(initialNodeArgs);
        node.config.verifyNewBlocks = false;
        Thread.sleep(100);
    }

    @After
    public void tearDown() throws Exception {
        node.kill();
        node.destroyPersistantData();
    }

    @Test
    public void getCurrentBlockHeight() {
        Block genesis = Block.generateGenesisBlock();

        Assert.assertTrue(BlockRESTWrapper.getCurrentBlockHeight(localHost).equals(BigInteger.ONE));

        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), BigInteger.TEN);
        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        Assert.assertTrue(BlockRESTWrapper.getCurrentBlockHeight(localHost).equals(BigInteger.ONE.add(BigInteger.ONE)));

        Block block2 = new Block(Arrays.asList(), genesis.header.getHash(), BigInteger.ONE);
        r = BlockRESTWrapper.newBlock(localHost, block2);
        Assert.assertTrue(!r.error);

        Assert.assertTrue(BlockRESTWrapper.getCurrentBlockHeight(localHost).equals(BigInteger.ONE.add(BigInteger.ONE)));
    }

    @Test
    public void getBlock() {
        Block genesis = Block.generateGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), BigInteger.TEN);
        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        Block genesisResponse = BlockRESTWrapper.getBlock(localHost, genesis.header.getHash()).block;
        Block nextBlockResponse = BlockRESTWrapper.getBlock(localHost, block1.header.getHash()).block;
        Assert.assertTrue(genesisResponse.equals(genesis));
        Assert.assertTrue(nextBlockResponse.equals(block1));
    }

    @Test
    public void getAllBlockHashes() {
        Block genesis = Block.generateGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), BigInteger.TEN);
        List<String> hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.header.getHash())));
        Assert.assertFalse(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));

        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.header.getHash())));
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));
    }
}