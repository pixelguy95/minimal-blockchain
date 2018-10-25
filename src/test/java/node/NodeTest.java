package node;

import apis.domain.Host;
import apis.utils.BlockRESTWrapper;
import apis.utils.TransactionRESTWrapper;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.block.Block;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * IMPORTANT!
 * You must port forward 13337/13340 to the computer you are running on
 */
public class NodeTest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1"};
    private static final String[] secondNodeArgs = new String[]{"-n", "localhost:13337", "-p", "13338", "-db", ".local-persistence-test2"};
    private static final String[] thirdNodeArgs = new String[]{"-n", "localhost:13338", "-p", "13339", "-db", ".local-persistence-test3"};
    private static final String[] fourthNodeArgs = new String[]{"-n", "localhost:13338", "-p", "13340", "-db", ".local-persistence-test4"};

    private Node node1;
    private Node node2;
    private Node node3;
    private Node node4;

    private PublicKey pub;

    @Before
    public void setUp() throws Exception {

        pub = ECKeyManager.generateNewKeyPair().getPublic();

        node1 = new Node(initialNodeArgs);
        node1.config.verifyNewBlocks = false;
        Thread.sleep(200);
        node2 = new Node(secondNodeArgs);
        node2.config.verifyNewBlocks = false;
        Thread.sleep(200);
        node3 = new Node(thirdNodeArgs);
        node3.config.verifyNewBlocks = false;
        Thread.sleep(200);
        node4 = new Node(fourthNodeArgs);
        node4.config.verifyNewBlocks = false;
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

        Block genesis = Block.generateGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pub);

        BlockRESTWrapper.newBlock(new Host(node1.config.outwardIP, node1.config.port), block1);
        Thread.sleep(100); //Wait for transaction to spread to all nodes

        assertTrue(node1.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
        assertTrue(node2.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
        assertTrue(node3.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
        assertTrue(node4.blockchain.getChain().containsKey(ByteBuffer.wrap(block1.header.getHash())));
    }
}