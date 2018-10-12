package apis.static_structures;

import db.DBSingletons;
import domain.block.Block;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BlockchainTest {

    @Before
    public void setUp() throws Exception {
        DBSingletons.init(".test-persist");
    }

    @Test
    public void addToChain() throws Exception {

        Block genesis = new Block(Arrays.asList(), DigestUtils.sha256("GENESIS BLOCK"), BigInteger.TEN);
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), BigInteger.TEN);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), BigInteger.TEN);

        Blockchain bc = Blockchain.getInstance();

        bc.addBlock(genesis);
        bc.addBlock(block1);
        bc.addBlock(block2);

        Assert.assertTrue(bc.getChain().size() == 3);
        Assert.assertTrue(bc.getLeafs().size() == 1);

        Assert.assertTrue(bc.getLeafs().get(ByteBuffer.wrap(block2.header.getHash())).height == 2);
        Assert.assertArrayEquals(bc.getLeafs().get(ByteBuffer.wrap(block2.header.getHash())).blockHeader.prevBlockHash, block1.header.getHash());
    }

    @Test
    public void addToChainSoItForks() throws Exception {
        Block genesis = new Block(Arrays.asList(), DigestUtils.sha256("GENESIS BLOCK"), BigInteger.TEN);
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), BigInteger.TEN);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), BigInteger.TEN);
        Block block3 = new Block(Arrays.asList(), block1.header.getHash(), BigInteger.ONE);

        Blockchain bc = Blockchain.getInstance();

        bc.addBlock(genesis);
        bc.addBlock(block1);
        bc.addBlock(block2);
        bc.addBlock(block3);

        Assert.assertTrue(bc.getChain().size() == 4);
        Assert.assertTrue(bc.getLeafs().size() == 2);

        Assert.assertTrue(bc.getLeafs().get(ByteBuffer.wrap(block2.header.getHash())).height == 2);
        Assert.assertTrue(bc.getLeafs().get(ByteBuffer.wrap(block3.header.getHash())).height == 2);

        Assert.assertArrayEquals(bc.getLeafs().get(ByteBuffer.wrap(block2.header.getHash())).blockHeader.prevBlockHash, block1.header.getHash());
        Assert.assertArrayEquals(bc.getLeafs().get(ByteBuffer.wrap(block3.header.getHash())).blockHeader.prevBlockHash, block1.header.getHash());
    }

    @After
    public void tearDown() throws Exception {
        DBSingletons.destroy(".test-persist");
        Blockchain.destroy();
    }
}