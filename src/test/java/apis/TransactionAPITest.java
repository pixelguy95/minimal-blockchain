package apis;

import apis.domain.Host;
import apis.utils.BlockRESTWrapper;
import apis.utils.TransactionValidator;
import domain.block.Block;
import domain.transaction.Transaction;
import node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.Assert.*;

public class TransactionAPITest {

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
    public void testTransactionValidation() throws InterruptedException {
        node.config.safeBlockLength = 4;
        KeyPair pair1 = ECKeyManager.generateNewKeyPair();
        Block genesis = node.blockchain.getGenesisBlock();
        Block block1 = new Block(Arrays.asList(), genesis.header.getHash(), pair.getPublic());
        Block block2 = new Block(Arrays.asList(), block1.header.getHash(), pair1.getPublic());
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

        Transaction spendingGenesisCoinbase = Transaction.makeTransactionFromOutputs(node.blockchain, genesisPair, node.utxo.getAllByPublicKey(genesisPair.getPublic()), pair1.getPublic(), 10);
        Transaction spendingBlock1Coinbase = Transaction.makeTransactionFromOutputs(node.blockchain, pair, node.utxo.getAllByPublicKey(pair.getPublic()), pair1.getPublic(), 10);

        TransactionValidator transactionValidator = new TransactionValidator(node.utxo, node.blockchain, node.transactionPool);
        assertTrue(transactionValidator.validateTransaction(spendingGenesisCoinbase).passed);
        assertTrue(transactionValidator.validateTransaction(spendingBlock1Coinbase).passed);

        Block block6 = new Block(Arrays.asList(spendingGenesisCoinbase), block5.header.getHash(), pair.getPublic());
        BlockRESTWrapper.newBlock(localHost, block6);

        assertFalse(transactionValidator.validateTransaction(spendingGenesisCoinbase).passed);
        assertEquals(transactionValidator.validateTransaction(spendingGenesisCoinbase).resaon, "One of the inputs is already used in a non-safe block");

        Transaction fake = Transaction.makeFakeTransaction(pair1.getPrivate(), pair1.getPublic());
        assertFalse(transactionValidator.validateTransaction(fake).passed);
        assertEquals(transactionValidator.validateTransaction(fake).resaon, "One of the inputs ws not found as UTXO");
    }
}