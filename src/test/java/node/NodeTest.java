package node;

import apis.domain.Host;
import apis.utils.wrappers.BlockRESTWrapper;
import apis.utils.wrappers.TransactionRESTWrapper;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.block.Block;
import domain.utxo.UTXOIdentifier;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * IMPORTANT!
 * You must port forward 13337/13340 to the computer you are running on
 */
public class NodeTest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1", "-nm"};
    private static final String[] secondNodeArgs = new String[]{"-n", "localhost:13337", "-p", "13338", "-db", ".local-persistence-test2", "-nm"};
    private static final String[] thirdNodeArgs = new String[]{"-n", "localhost:13338", "-p", "13339", "-db", ".local-persistence-test3", "-nm"};
    private static final String[] fourthNodeArgs = new String[]{"-n", "localhost:13338", "-p", "13340", "-db", ".local-persistence-test4", "-nm"};
    private static final String[] lonelyNode = new String[]{"-n", "localhost:13338", "-p", "13341", "-db", ".local-persistence-test5", "-nm"};


    private Node node1;
    private Node node2;
    private Node node3;
    private Node node4;

    private KeyPair pair;

    @Before
    public void setUp() throws Exception {

        pair = ECKeyManager.generateNewKeyPair();

        node1 = new Node(initialNodeArgs);
        node1.config.validateNewBlocks = false;
        Thread.sleep(200);
        node2 = new Node(secondNodeArgs);
        node2.config.validateNewBlocks = false;
        Thread.sleep(200);
        node3 = new Node(thirdNodeArgs);
        node3.config.validateNewBlocks = false;
        Thread.sleep(200);
        node4 = new Node(fourthNodeArgs);
        node4.config.validateNewBlocks = false;
        Thread.sleep(200);
    }

    @After
    public void tearDown() throws Exception {
        node1.kill();
        node2.kill();
        node3.kill();
        node4.kill();

        node1.destroyPersistantData();
        node2.destroyPersistantData();
        node3.destroyPersistantData();
        node4.destroyPersistantData();
    }

    @Test
    public void testNetworkBuilding() {
        Assert.assertTrue(node1.knownNodesList.getKnownNodes().size() == 3);
        Assert.assertTrue(node2.knownNodesList.getKnownNodes().size() == 3);
        Assert.assertTrue(node3.knownNodesList.getKnownNodes().size() == 3);
        Assert.assertTrue(node4.knownNodesList.getKnownNodes().size() == 3);

        Assert.assertTrue(node1.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13338)));
        Assert.assertTrue(node1.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13339)));
        Assert.assertTrue(node1.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13340)));

        Assert.assertTrue(node2.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13337)));
        Assert.assertTrue(node2.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13339)));
        Assert.assertTrue(node2.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13340)));

        Assert.assertTrue(node3.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13337)));
        Assert.assertTrue(node3.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13338)));
        Assert.assertTrue(node3.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13340)));

        Assert.assertTrue(node4.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13337)));
        Assert.assertTrue(node4.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13338)));
        Assert.assertTrue(node4.knownNodesList.getKnownNodes().contains(new Host(node1.config.outwardIP, 13339)));
    }

    /**
     * This will work until we add transaction validation, then it will need modifications
     *
     * Sends a fake transaction to node 1, then waits for a bit, then checks if all nodes has that transaction in their
     * respective transaction pool
     * @throws InterruptedException
     */
    @Test
    public void transactionPoolShare() throws InterruptedException {

        node1.config.validateNewTransactions = false;
        node2.config.validateNewTransactions = false;
        node3.config.validateNewTransactions = false;
        node4.config.validateNewTransactions = false;

        byte[] fakePartial = DigestUtils.sha256("This will be the partial hash".getBytes());
        byte[] fakeTransactionHash = DigestUtils.sha256("This will be the full hash".getBytes());

        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(fakePartial, kp.getPrivate());
        byte[] publicKey = kp.getPublic().getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        Input input = new Input(fakeTransactionHash, 0, scriptSig.length, scriptSig, 0xFFFFFFFF);
        List<Input> inputs = Arrays.asList(input);

        Output output = new Output(10, scriptPubKey.length, scriptPubKey);
        List<Output> outputs = Arrays.asList(output);

        Transaction t = new Transaction(1, (short) 0, 1, 1, inputs, outputs, null, 0xFFFFFFFF);

        TransactionRESTWrapper.sendTransaction(new Host(node1.config.outwardIP, node1.config.port), t);
        Thread.sleep(100); //Wait for transaction to spread to all nodes

        Assert.assertTrue(node1.transactionPool.has(t.fullHash()));
        Assert.assertTrue(node2.transactionPool.has(t.fullHash()));
        Assert.assertTrue(node3.transactionPool.has(t.fullHash()));
        Assert.assertTrue(node4.transactionPool.has(t.fullHash()));
    }


    /**
     * This will work until we add transaction validation, then it will need modifications
     *
     * Sends a fake transaction to node 1, then waits for a bit, then checks if all nodes has that transaction in their
     * respective transaction pool
     * @throws InterruptedException
     */
    @Test
    public void blockRetransmission() throws InterruptedException {

        Block genesis = Block.generateGenesisBlock(node1.blockchain);
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());

        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block1);
        Thread.sleep(100); //Wait for block to spread to all nodes

        assertTrue(node1.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
        assertTrue(node2.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
        assertTrue(node3.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
        assertTrue(node4.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
    }


    @Test
    public void utxoAddedToAllNodes() throws InterruptedException {

        node1.config.safeBlockLength = 4;
        node2.config.safeBlockLength = 4;
        node3.config.safeBlockLength = 4;
        node4.config.safeBlockLength = 4;

        Transaction t = Transaction.makeFakeTransaction(pair.getPrivate(), pair.getPublic());

        Block genesis = node1.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(t), genesis.header.getHash(), pair.getPublic());

        //New
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), ECKeyManager.generateNewKeyPair().getPublic());
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pair.getPublic());
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pair.getPublic());
        Block block5 = new Block(Arrays.asList(), block4.header.getHash(), pair.getPublic());


        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block1);
        Thread.sleep(70); //Wait for block to spread to all nodes
        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block2);
        Thread.sleep(70); //Wait for block to spread to all nodes
        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block3);
        Thread.sleep(70); //Wait for block to spread to all nodes
        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block4);
        Thread.sleep(70); //Wait for block to spread to all nodes

        assertTrue(node1.utxo.getAll().size() == 1);
        assertTrue(node2.utxo.getAll().size() == 1);
        assertTrue(node3.utxo.getAll().size() == 1);
        assertTrue(node4.utxo.getAll().size() == 1);

        assertTrue(node1.utxo.has(new UTXOIdentifier(genesis.coinbase.fullHash(), 0)));
        assertFalse(node1.utxo.has(new UTXOIdentifier(block1.coinbase.fullHash(), 0)));

        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block5);
        Thread.sleep(70); //Wait for block to spread to all nodes

        assertTrue(node1.utxo.getAll().size() == 3);
        assertTrue(node2.utxo.getAll().size() == 3);
        assertTrue(node3.utxo.getAll().size() == 3);
        assertTrue(node4.utxo.getAll().size() == 3);

        assertTrue(node1.utxo.has(new UTXOIdentifier(genesis.coinbase.fullHash(), 0)));
        assertTrue(node1.utxo.has(new UTXOIdentifier(block1.coinbase.fullHash(), 0)));
        assertTrue(node1.utxo.has(new UTXOIdentifier(t.fullHash(), 0)));
        assertFalse(node1.utxo.has(new UTXOIdentifier(block2.coinbase.fullHash(), 0)));
    }

    @Test
    public void testLonelyNodeNotInitial() throws InterruptedException, IOException {
        node1.kill();
        node2.kill();
        node3.kill();
        node4.kill();
        node1.destroyPersistantData();
        node2.destroyPersistantData();
        node3.destroyPersistantData();
        node4.destroyPersistantData();

        Node lNode = new Node(lonelyNode);
        Thread.sleep(2000);
        assertFalse(lNode.isRunning.get());

        lNode.destroyPersistantData();
    }

    /**
     * At time of writing the largest and most time consuming test of all.
     *
     * Starts by adding a block to make sure that it propagates as it should.
     * Then kills node 2 and 3
     * Adds another block to the network. THe remaining nodes should be able to handle the change in network structure
     * Makes sure tht the remaining node only knows of each other.
     * Adds another block to make the following tasks a bit less trivial.
     * Starts up the killed nodes again
     * Checks that all the nodes know of each pother (the network is complete again)
     * Checks that all the blocks added while 2 and 3 were gone is synced across the network.
     * Adds another block just to make sure that everything is alright.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testDisconnectingNodes() throws InterruptedException, IOException {
        Block genesis = node1.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pair.getPublic());
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pair.getPublic());
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pair.getPublic());

        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block1);
        Thread.sleep(70); //Wait for block to spread to all nodes

        assertTrue(node1.blockchain.getChain().size() == 2);
        assertTrue(node2.blockchain.getChain().size() == 2);
        assertTrue(node3.blockchain.getChain().size() == 2);
        assertTrue(node4.blockchain.getChain().size() == 2);

        System.out.println("Killing node 2");
        node2.kill();
        System.out.println("Killing node 3");
        node3.kill();

        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block2);
        Thread.sleep(70); //Wait for block to spread to all nodes

        assertTrue(node1.knownNodesList.getKnownNodes().size() == 1);
        assertTrue(node4.knownNodesList.getKnownNodes().size() == 1);

        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block3);
        Thread.sleep(70); //Wait for block to spread to all nodes

        node2 = new Node(secondNodeArgs);
        Thread.sleep(300);
        node3 = new Node(thirdNodeArgs);
        Thread.sleep(300);

        assertTrue(node1.knownNodesList.getKnownNodes().size() == 3);
        assertTrue(node2.knownNodesList.getKnownNodes().size() == 3);
        assertTrue(node3.knownNodesList.getKnownNodes().size() == 3);
        assertTrue(node4.knownNodesList.getKnownNodes().size() == 3);

        assertTrue(node1.blockchain.getChain().size() == 4);
        assertTrue(node2.blockchain.getChain().size() == 4);
        assertTrue(node3.blockchain.getChain().size() == 4);
        assertTrue(node4.blockchain.getChain().size() == 4);

        BlockRESTWrapper.newBlock(new Host(node2.config.outwardIP, node1.config.port), block4);
        Thread.sleep(70); //Wait for block to spread to all nodes

        assertTrue(node1.blockchain.getChain().size() == 5);
        assertTrue(node2.blockchain.getChain().size() == 5);
        assertTrue(node3.blockchain.getChain().size() == 5);
        assertTrue(node4.blockchain.getChain().size() == 5);

        assertTrue(node1.blockchain.getLeafs().size() == 1);
        assertTrue(node2.blockchain.getLeafs().size() == 1);
        assertTrue(node3.blockchain.getLeafs().size() == 1);
        assertTrue(node4.blockchain.getLeafs().size() == 1);
    }
}