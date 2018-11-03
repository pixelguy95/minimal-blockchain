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

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TransactionAPITest {

    private static final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-test1",  "-nm"};
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
        Block block1 = new Block.Builder().putTransactions(Arrays.asList()).generateCoinBase(pair.getPublic(), 1, node.utxo).generateHeader(node.blockchain).end();
        BlockRESTWrapper.newBlock(localHost, block1);
        Block block2 = new Block.Builder().putTransactions(Arrays.asList()).generateCoinBase(pair1.getPublic(), 2, node.utxo).generateHeader(node.blockchain).end();
        BlockRESTWrapper.newBlock(localHost, block2);
        Block block3 = new Block.Builder().putTransactions(Arrays.asList()).generateCoinBase(pair.getPublic(), 3, node.utxo).generateHeader(node.blockchain).end();
        BlockRESTWrapper.newBlock(localHost, block3);
        Block block4 = new Block.Builder().putTransactions(Arrays.asList()).generateCoinBase(pair.getPublic(), 4, node.utxo).generateHeader(node.blockchain).end();
        BlockRESTWrapper.newBlock(localHost, block4);
        Block block5 = new Block.Builder().putTransactions(Arrays.asList()).generateCoinBase(pair.getPublic(), 5, node.utxo).generateHeader(node.blockchain).end();
        BlockRESTWrapper.newBlock(localHost, block5);

        TransactionValidator transactionValidator = new TransactionValidator(node.utxo, node.blockchain, node.transactionPool);
        KeyPair genesisPair = ECKeyManager.loadPairFromFile(".genesis.key.pair");
        assertTrue(node.utxo.getAllByPublicKey(genesisPair.getPublic()).size() == 1);

        BigInteger tooLarge = new BigInteger("5000000001");

        Transaction spendingGenesisCoinbase = Transaction.makeTransactionFromOutputs(node.blockchain, genesisPair, node.utxo.getAllByPublicKey(genesisPair.getPublic()), pair1.getPublic(), 10);
        Transaction spendingBlock1Coinbase = Transaction.makeTransactionFromOutputs(node.blockchain, pair, node.utxo.getAllByPublicKey(pair.getPublic()), pair1.getPublic(), 100);

        spendingGenesisCoinbase.outputs.get(1).amount = tooLarge.longValue();

        assertFalse(transactionValidator.validate(spendingGenesisCoinbase).passed);
        assertEquals(transactionValidator.validate(spendingGenesisCoinbase).resaon, "The sum of inputs was not enough to cover sum of outputs");

        spendingGenesisCoinbase = Transaction.makeTransactionFromOutputs(node.blockchain, genesisPair, node.utxo.getAllByPublicKey(genesisPair.getPublic()), pair1.getPublic(), 10);

        assertTrue(transactionValidator.validate(spendingGenesisCoinbase).passed);
        assertTrue(transactionValidator.validate(spendingBlock1Coinbase).passed);

        Block block6 = new Block(Arrays.asList(spendingGenesisCoinbase), block5.header.getHash(), pair.getPublic());
        BlockRESTWrapper.newBlock(localHost, block6);

        assertFalse(transactionValidator.validate(spendingGenesisCoinbase).passed);
        assertEquals(transactionValidator.validate(spendingGenesisCoinbase).resaon, "One of the inputs is already used in a non-safe block");

        Transaction fake = Transaction.makeFakeTransaction(pair1.getPrivate(), pair1.getPublic());
        assertFalse(transactionValidator.validate(fake).passed);
        assertEquals(transactionValidator.validate(fake).resaon, "One of the inputs ws not found as UTXO");


    }
}