package node.tasks;

import apis.domain.Host;
import apis.domain.responses.BooleanResponse;
import apis.utils.BlockRESTWrapper;
import domain.block.Block;
import domain.transaction.Transaction;
import node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BlockSyncTest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1"};
    private static final String[] secondNodeArgs = new String[]{"-n", "localhost:13337", "-p", "13338", "-db", ".local-persistence-test2"};
    private static final String[] thirdNodeArgs = new String[]{"-n", "localhost:13337", "-p", "13339", "-db", ".local-persistence-test3"};
    private static final String[] fourthNodeArgs = new String[]{"-n", "localhost:13337", "-p", "13340", "-db", ".local-persistence-test4"};

    private Node node1;
    private Node node2;
    private Node node3;
    private Node node4;

    private KeyPair pair;

    public Host localHost = new Host("localhost:13337");

    @Before
    public void setUp() throws Exception {

        pair = ECKeyManager.generateNewKeyPair();

        System.out.println("NODE 1");
        node1 = new Node(initialNodeArgs);
        node1.config.verifyNewBlocks = false;
        Thread.sleep(200);

        System.out.println("NODE 2");
        node2 = new Node(secondNodeArgs);
        node2.config.verifyNewBlocks = false;
        Thread.sleep(200);

        System.out.println("NODE 3");
        node3 = new Node(thirdNodeArgs);
        node3.config.verifyNewBlocks = false;
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
    public void sync() throws InterruptedException, IOException {

        Block genesis = node1.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pair.getPublic());
        Block block3 = new Block(Arrays.asList(), block2.header.getHash(), pair.getPublic());
        Block block4 = new Block(Arrays.asList(), block3.header.getHash(), pair.getPublic());
        Block block4alt = new Block(Arrays.asList(Transaction.makeFakeTransaction(pair.getPrivate(), pair.getPublic())), block3.header.getHash(), pair.getPublic());
        Block block5 = new Block(Arrays.asList(), block4.header.getHash(), pair.getPublic());
        Block block6 = new Block(Arrays.asList(), block5.header.getHash(), pair.getPublic());
        Block block7 = new Block(Arrays.asList(), block6.header.getHash(), pair.getPublic());
        Block block8 = new Block(Arrays.asList(), block7.header.getHash(), pair.getPublic());

        BlockRESTWrapper.newBlock(localHost, block1);
        BlockRESTWrapper.newBlock(localHost, block2);
        BlockRESTWrapper.newBlock(localHost, block3);
        BlockRESTWrapper.newBlock(localHost, block4alt);
        BlockRESTWrapper.newBlock(localHost, block4);
        BlockRESTWrapper.newBlock(localHost, block5);
        BlockRESTWrapper.newBlock(localHost, block6);
        BlockRESTWrapper.newBlock(localHost, block7);
        BlockRESTWrapper.newBlock(localHost, block8);


        System.out.println("NODE 4");
        node4 = new Node(fourthNodeArgs);
        node4.config.verifyNewBlocks = false;
        Thread.sleep(200);

        assertEquals(node4.blockchain.getChain().size(), 10);
        node3.kill();

        Block block8alt = new Block(Arrays.asList(Transaction.makeFakeTransaction(pair.getPrivate(), pair.getPublic())), block7.header.getHash(), pair.getPublic());
        Block block9 = new Block(Arrays.asList(), block8alt.header.getHash(), pair.getPublic());
        Block block10 = new Block(Arrays.asList(), block9.header.getHash(), pair.getPublic());
        Block block11 = new Block(Arrays.asList(), block10.header.getHash(), pair.getPublic());
        Block block12 = new Block(Arrays.asList(), block11.header.getHash(), pair.getPublic());

        BlockRESTWrapper.newBlock(localHost, block8alt);
        BlockRESTWrapper.newBlock(localHost, block9);
        BlockRESTWrapper.newBlock(localHost, block10);
        BlockRESTWrapper.newBlock(localHost, block11);
        BlockRESTWrapper.newBlock(localHost, block12);

        node3 = new Node(thirdNodeArgs);

        assertEquals(node3.blockchain.getChain().size(), 15);
        assertEquals(node3.blockchain.getTopBlock(), block12);
    }
}