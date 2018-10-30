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
import domain.transaction.Transaction;
import node.Node;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;
import utils.RESTUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.Assert.*;

public class BlockAPITest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1"};
    public Node node;

    public Host localHost = new Host("localhost:13337");

    private KeyPair pair;

    @Before
    public void setUp() throws Exception {
        pair = ECKeyManager.generateNewKeyPair();

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
        Block genesis = node.blockchain.getGenesisBlock();

        Assert.assertTrue(BlockRESTWrapper.getCurrentBlockHeight(localHost).equals(BigInteger.ONE));

        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        Assert.assertTrue(BlockRESTWrapper.getCurrentBlockHeight(localHost).equals(BigInteger.ONE.add(BigInteger.ONE)));

        Block block2 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        r = BlockRESTWrapper.newBlock(localHost, block2);
        Assert.assertTrue(!r.error);

        Assert.assertTrue(BlockRESTWrapper.getCurrentBlockHeight(localHost).equals(BigInteger.ONE.add(BigInteger.ONE)));
    }

    @Test
    public void getBlock() {
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        Block genesisResponse = BlockRESTWrapper.getBlock(localHost, genesis.header.getHash()).block;
        Block nextBlockResponse = BlockRESTWrapper.getBlock(localHost, block1.header.getHash()).block;
        Assert.assertTrue(genesisResponse.equals(genesis));
        Assert.assertTrue(nextBlockResponse.equals(block1));
    }

    @Test
    public void getAllBlockHashes() {
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        List<String> hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.header.getHash())));
        Assert.assertFalse(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));

        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.header.getHash())));
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));
    }

    @Test
    public void getAllBlockHashesWithHeight() {
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        List<String> hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.header.getHash())));
        Assert.assertFalse(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));

        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);

        hashes = BlockRESTWrapper.getAllBlockHashesFromHeight(localHost, 1).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));
        Assert.assertEquals(hashes.size(), 1);
    }


    @Test
    public void getAllBlockHashesCheckSorted() {
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pair.getPublic());
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pair.getPublic());
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pair.getPublic());

        List<String> hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        Assert.assertTrue(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.header.getHash())));
        Assert.assertFalse(hashes.contains(Base64.getUrlEncoder().withoutPadding().encodeToString(block1.header.getHash())));

        BooleanResponse r = BlockRESTWrapper.newBlock(localHost, block1);
        Assert.assertTrue(!r.error);
        r = BlockRESTWrapper.newBlock(localHost, block2);
        Assert.assertTrue(!r.error);
        r = BlockRESTWrapper.newBlock(localHost, block3);
        Assert.assertTrue(!r.error);
        r = BlockRESTWrapper.newBlock(localHost, block4);
        Assert.assertTrue(!r.error);

        hashes = BlockRESTWrapper.getAllBlockHashes(localHost).hashes;
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(0))), ByteBuffer.wrap(genesis.header.getHash()));
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(1))), ByteBuffer.wrap(block1.header.getHash()));
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(2))), ByteBuffer.wrap(block2.header.getHash()));
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(3))), ByteBuffer.wrap(block3.header.getHash()));
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(4))), ByteBuffer.wrap(block4.header.getHash()));

        hashes = BlockRESTWrapper.getAllBlockHashesFromHeight(localHost, 3).hashes;
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(0))), ByteBuffer.wrap(block3.header.getHash()));
        assertEquals(ByteBuffer.wrap(Base64.getUrlDecoder().decode(hashes.get(1))), ByteBuffer.wrap(block4.header.getHash()));
    }

    /**
     * This test relies on the saved genesis key pair file.
     * Without it the test will fail
     */
    @Test
    public void busyUTXOsAndBlockPruning() throws InterruptedException {

        node.config.safeBlockLength = 4;
        KeyPair pair1 = ECKeyManager.generateNewKeyPair();
        //Transaction t = Transaction.makeFakeTransaction(pair.getPrivate(), pair1.getPublic());

        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pair.getPublic());
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pair.getPublic());
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pair.getPublic());
        Block block5 = new Block(Arrays.asList(), block4.header.getHash(), pair.getPublic());

        BlockRESTWrapper.newBlock(localHost, block1);
        BlockRESTWrapper.newBlock(localHost, block2);
        BlockRESTWrapper.newBlock(localHost, block3);
        BlockRESTWrapper.newBlock(localHost, block4);
        BlockRESTWrapper.newBlock(localHost, block5);

        KeyPair genesisPair = ECKeyManager.loadPairFromFile(".genesis.key.pair");
        assertTrue(node.utxo.getAllByPublicKey(genesisPair.getPublic()).size() == 1);
        System.out.println(node.utxo.getAllByPublicKey(genesisPair.getPublic()).get(0).outputIndex);
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(node.utxo.getAllByPublicKey(genesisPair.getPublic()).get(0).txid));
        System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(genesis.coinbase.fullHash()));

        Transaction spendingGenesisCoinbase = Transaction.makeTransactionFromOutputs(node.blockchain, genesisPair, node.utxo.getAllByPublicKey(genesisPair.getPublic()), pair1.getPublic(), 10);
        assertTrue(spendingGenesisCoinbase != null);
        assertTrue(spendingGenesisCoinbase.outputs.size() == 2);
        assertTrue(spendingGenesisCoinbase.inputs.size() == 1);
        assertEquals(ByteBuffer.wrap(spendingGenesisCoinbase.inputs.get(0).transactionHash), ByteBuffer.wrap(genesis.coinbase.fullHash()));

        Transaction spendingBlock1Coinbase = Transaction.makeTransactionFromOutputs(node.blockchain, pair, node.utxo.getAllByPublicKey(pair.getPublic()), pair1.getPublic(), 10);
        Block block6 = new Block(Arrays.asList(spendingGenesisCoinbase), block5.header.getHash(), pair.getPublic());
        BlockRESTWrapper.newBlock(localHost, block6);
        Thread.sleep(70);

        Block block6alt = new Block(Arrays.asList(spendingBlock1Coinbase), block5.header.getHash(), pair.getPublic());
        BlockRESTWrapper.newBlock(localHost, block6alt);
        Thread.sleep(70);

        assertTrue(node.utxo.getAll().size() == 2);
        assertTrue(node.utxo.busy.size() == 2);

        Block block7 = new Block(Arrays.asList(), block6.header.getHash(), pair.getPublic());
        Block block8 = new Block(Arrays.asList(), block7.header.getHash(), pair.getPublic());
        Block block9 = new Block(Arrays.asList(), block8.header.getHash(), pair.getPublic());
        Block block10 = new Block(Arrays.asList(), block9.header.getHash(), pair.getPublic());
        Block block11 = new Block(Arrays.asList(), block10.header.getHash(), pair.getPublic());

        BlockRESTWrapper.newBlock(localHost, block7);
        BlockRESTWrapper.newBlock(localHost, block8);
        BlockRESTWrapper.newBlock(localHost, block9);
        BlockRESTWrapper.newBlock(localHost, block10);
        BlockRESTWrapper.newBlock(localHost, block11);

        while(node.utxo.busy.size() > 0)
            Thread.sleep(100);

        assertEquals(node.utxo.busy.size(), 0);
    }
}