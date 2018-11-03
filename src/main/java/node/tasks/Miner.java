package node.tasks;

import apis.domain.Host;
import apis.static_structures.Blockchain;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import apis.utils.BlockRESTWrapper;
import domain.block.Block;
import domain.block.BlockHeader;
import domain.transaction.Transaction;
import node.Config;
import security.ECKeyManager;
import utils.DifficultyAdjustment;
import utils.DifficultyAdjustmentRedux;

import java.math.BigInteger;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Miner extends AbstractTask {

    private Blockchain blockchain;
    private TransactionPool transactionPool;
    private UTXO utxo;
    private Config config;

    public Miner(AtomicBoolean keepAlive, Blockchain blockchain, TransactionPool transactionPool, UTXO utxo, Config config) {
        super(keepAlive);
        this.blockchain = blockchain;
        this.transactionPool = transactionPool;
        this.utxo = utxo;
        this.config = config;
    }

    @Override
    public void run() {

        AtomicBoolean keepLooking = new AtomicBoolean(true);
        AtomicBoolean cancelWatch = new AtomicBoolean(false);

        new Thread(() -> {
            long height = blockchain.getBestHeight();
            while(keepAlive.get()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(cancelWatch.get()) {
                    height = blockchain.getBestHeight();
                    cancelWatch.set(false);
                    continue;
                }

                if(height!=blockchain.getBestHeight()) {
                    keepLooking.set(false);
                    height=blockchain.getBestHeight();
                }
            }
        }).start();

        System.out.println("Start of mining");
        while(keepAlive.get()) {

            KeyPair pair = ECKeyManager.generateNewKeyPair();
            keepLooking.set(true);

            Block candidate = constructCandidate();

            long start = System.currentTimeMillis();
            boolean iMined = mineBlock(candidate, keepLooking, cancelWatch);
            System.out.println("TIME " + ((double)(System.currentTimeMillis() - start) / 1000.0));
            System.out.println("CHAIN SIZE: " + blockchain.getChain().size());

            if(iMined)
                BlockRESTWrapper.newBlock(new Host("localhost", config.port), candidate);

        }
    }

    private Block constructCandidate() {

        List<Transaction> hunderedTransactions = transactionPool.getNTransactions(100);

        Block candidate = new Block.Builder()
                .putTransactions(hunderedTransactions)
                .generateCoinBase(config.miningPublicKey, blockchain.getBestHeight(), utxo)
                .generateHeader(blockchain).end();

        return candidate;
    }

    protected boolean mineBlock(Block candidate, AtomicBoolean keepLooking, AtomicBoolean cancelWatch)  {
        BlockHeader header = candidate.header;
        BigInteger target = DifficultyAdjustmentRedux.toTarget(header.bits);

        header.rendomizeNonce(); //So that all nodes doesn't start from the same nonce

        int searched = 0;
        while(keepLooking.get()){
            searched++;

            byte[] hash = header.getHash();
            if(new BigInteger(ByteBuffer.allocate(1+hash.length).put((byte) 0x00).put(hash).array()).compareTo(target) < 0) {
                break;
            }

            header.incrementNonce();
        }

        if(keepLooking.get()) {
            cancelWatch.set(true);
            System.out.println("BLOCK SUCCESSFULLY MINED WITH HASH");
            System.out.println(searched);
            System.out.println(new BigInteger(header.getHash()).toString(16));
            System.out.println(target.toString(16));
            System.out.println(keepLooking.get());
            return true;
        }

        return false;
    }
}
