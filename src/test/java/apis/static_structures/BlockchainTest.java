package apis.static_structures;

import db.DBHolder;
import domain.block.Block;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.utxo.UTXOIdentifier;
import node.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Map;

public class BlockchainTest {

    private Blockchain bc;
    private DBHolder dbs;
    private PublicKey pub;

    @Before
    public void setUp() throws Exception {
        dbs = new DBHolder(".test-persist");
        Config config = new Config(new String[]{});
        config.allowOrphanBlocks = true;
        bc = new Blockchain(dbs.getBlockDB(), dbs.getBlockHeaderDB(), dbs.getMetaDB(), config);
        bc.addBlock(Block.generateGenesisBlock());
        pub = ECKeyManager.generateNewKeyPair().getPublic();
    }

    @Test
    public void addToChain() throws Exception {

        Block genesis = bc.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pub);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pub);

        bc.addBlock(block1);
        bc.addBlock(block2);

        Assert.assertTrue(bc.getChain().size() == 3);
        Assert.assertTrue(bc.getLeafs().size() == 1);

        Assert.assertTrue(bc.getLeafs().get(ByteBuffer.wrap(block2.header.getHash())).height == 2);
        Assert.assertArrayEquals(bc.getLeafs().get(ByteBuffer.wrap(block2.header.getHash())).blockHeader.prevBlockHash, block1.header.getHash());
    }

    @Test
    public void addToChainSoItForks() throws Exception {
        KeyPair kp = ECKeyManager.generateNewKeyPair();

        Block genesis = bc.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pub);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pub);
        Block block3 = new Block(Arrays.asList(Transaction.makeFakeTransaction(kp.getPrivate(), kp.getPublic())), block1.header.getHash(), pub);

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

    @Test
    public void buildInitialBLockChainOutOfOrder() throws Exception {

        Block genesis = bc.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pub);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pub);
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pub);

        bc.addBlock(block3);
        bc.addBlock(block2);
        bc.addBlock(block1);

        Assert.assertTrue(bc.getChain().size() == 4);
        Assert.assertTrue(bc.getLeafs().size() == 1);

        Assert.assertTrue(bc.getLeafs().get(ByteBuffer.wrap(block3.header.getHash())).height == 3);
        Assert.assertArrayEquals(bc.getLeafs().get(ByteBuffer.wrap(block3.header.getHash())).blockHeader.prevBlockHash, block2.header.getHash());
    }

    @Test
    public void testUTXOCandidates() throws Exception {
        KeyPair kp = ECKeyManager.generateNewKeyPair();

        Block genesis = bc.getGenesisBlock();
        Transaction t = Transaction.makeFakeTransaction(kp.getPrivate(), kp.getPublic());
        Block block1 = new Block(Arrays.asList(t), genesis.header.getHash(), pub);
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pub);
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pub);
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pub);
        Block block5 = new Block(Arrays.asList(), block4.header.getHash(), pub);

        bc.addBlock(block1);
        Assert.assertTrue(bc.getUTXOCandidates().size() == 0);
        bc.addBlock(block2);
        Assert.assertTrue(bc.getUTXOCandidates().size() == 0);
        bc.addBlock(block3);
        Assert.assertTrue(bc.getUTXOCandidates().size() == 0);
        bc.addBlock(block4);
        Assert.assertTrue(bc.getUTXOCandidates().size() == 1);

        Map<UTXOIdentifier, Output> utxo = bc.getUTXOCandidates();

        Assert.assertTrue(utxo.containsKey(new UTXOIdentifier(genesis.coinbase.fullHash(), 0)));
        Assert.assertFalse(utxo.containsKey(new UTXOIdentifier(block1.coinbase.fullHash(), 0)));

        bc.addBlock(block5);
        Assert.assertTrue(bc.getUTXOCandidates().size() == 2);

        utxo = bc.getUTXOCandidates();
        Assert.assertTrue(utxo.containsKey(new UTXOIdentifier(block1.coinbase.fullHash(), 0)));
        Assert.assertTrue(utxo.containsKey(new UTXOIdentifier(t.fullHash(), 0)));
    }

    @After
    public void tearDown() throws Exception {
        dbs.destroy(".test-persist");
    }



}