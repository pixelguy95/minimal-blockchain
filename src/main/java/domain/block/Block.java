package domain.block;

import apis.static_structures.Blockchain;
import apis.static_structures.UTXO;
import domain.Validatable;
import domain.transaction.CoinbaseTransaction;
import domain.transaction.Output;
import domain.transaction.Transaction;
import org.apache.commons.codec.digest.DigestUtils;
import script.ScriptBuilder;
import security.ECKeyManager;
import utils.DifficultyAdjustmentRedux;
import utils.MerkleTreeUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Serializable , Validatable {

    public static final long INITIAL_REWARD = (long)((long)5000000 * (long)1000);

    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;
    public CoinbaseTransaction coinbase;

    /**
     * This constructor is really dated and should not be used
     * @param transactions
     * @param prevBlockHash
     * @param pub
     */
    @Deprecated
    public Block(List<Transaction> transactions, byte[] prevBlockHash, PublicKey pub) {
        this.transactions = transactions;
        this.transactionCounter = transactions.size();

        generateCoinBaseTransaction(pub);
        byte[] merkeleTreeRoot = generateMerkeleRoot();

        header = new BlockHeader(1, prevBlockHash, merkeleTreeRoot, 0);
    }

    private Block(int blockSize, BlockHeader header, long transactionCounter, List<Transaction> transactions, CoinbaseTransaction coinbase) {
        this.blockSize = blockSize;
        this.header = header;
        this.transactionCounter = transactionCounter;
        this.transactions = transactions;
        this.coinbase = coinbase;
    }

    /**
     * The coinbase transaction should be to the miners address and the amount should be equal
     * to the difference between all the inputs and all the outputs + the block reward.
     */
    public void generateCoinBaseTransaction(PublicKey pub) {

        //TODO: calculate the amount in the output
        long amount = 0 + 50;
        byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(pub);
        int scriptPubKeyLength = scriptPubKey.length;

        coinbase = new CoinbaseTransaction(1, (short) 0, new Output(amount, scriptPubKeyLength, scriptPubKey), null, 0xFFFFFFFF);
    }

    /**
     * WARNING: This might be completly wrong, dont be afraid to change, make sure to sync all tests after however
     * @return
     */
    public byte[] generateMerkeleRoot() {
        List<byte[]> transactionBytes = transactions.stream().map(t->t.serialize()).collect(Collectors.toList());
        transactionBytes.add(coinbase.serialize());
        return MerkleTreeUtils.createMerkle(transactionBytes);
    }

    public boolean equals(Object o) {
        Block otherBlock = (Block)o;

        return (ByteBuffer.wrap(this.header.getHash()).equals(ByteBuffer.wrap(otherBlock.header.getHash()))) &&
                this.blockSize == otherBlock.blockSize &&
                this.transactionCounter == otherBlock.transactionCounter &&
                this.transactions.equals(otherBlock.transactions) &&
                this.coinbase.equals(otherBlock.coinbase);
    }

    public static Block generateGenesisBlock(Blockchain blockchain) {
        PublicKey pub = ECKeyManager.bytesToPublicKey(Base64.getUrlDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAECibpSuVLXUv-dNRPpDJ7H416s8g0e3uqGMkTL23OOMewtkeOPH8GgByAu7-acla3bmORGH5GadbUEjBk9YE79A"));

        Block genesis = new Block.Builder()
                .putTransactions(Arrays.asList())
                .generateCoinBaseWithoutFees(pub, 0)
                .generateHeaderWithGivenPrevBlock(blockchain, DigestUtils.sha256("GENESIS BLOCK")).end();

        return genesis;
    }

    public static class Builder {
        public int blockSize;
        public BlockHeader header;
        public long transactionCounter;
        public List<Transaction> transactions;
        public CoinbaseTransaction coinbase;

        public Builder() {
            blockSize = 0;
            transactionCounter = 0;
            transactions = new ArrayList<>();
        }

        public Builder putTransactions(List<Transaction> transactions){
            this.transactions.addAll(transactions);
            this.transactionCounter+=transactions.size();
            return this;
        }

        public Builder generateCoinBase(PublicKey pub, long bestHeight, UTXO utxo) {
            long reward = Block.INITIAL_REWARD / (long)(Math.pow(2.0, Math.floor(bestHeight / 210_000)));
            long totalFees = transactions.stream().mapToLong(t->t.getFee(utxo)).sum();
            long amount = reward + totalFees;

            byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(pub);
            int scriptPubKeyLength = scriptPubKey.length;
            Output coinbaseOutput = new Output(amount, scriptPubKeyLength, scriptPubKey);
            this.coinbase = new CoinbaseTransaction(1, (short) 0, coinbaseOutput, null, 0xFFFFFFFF);

            return this;
        }

        public Builder generateCoinBaseWithoutFees(PublicKey pub, long bestHeight) {
            long reward = Block.INITIAL_REWARD / (long)(Math.pow(2.0, Math.floor(bestHeight / 210_000)));
            long amount = reward;

            byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(pub);
            int scriptPubKeyLength = scriptPubKey.length;
            Output coinbaseOutput = new Output(amount, scriptPubKeyLength, scriptPubKey);
            this.coinbase = new CoinbaseTransaction(1, (short) 0, coinbaseOutput, null, 0xFFFFFFFF);

            return this;
        }

        public Builder generateHeader(Blockchain blockchain) {
            List<Transaction> transactionsPlusCoinBase = new ArrayList<>();
            transactionsPlusCoinBase.addAll(transactions);
            transactionsPlusCoinBase.add(coinbase);

            this.header = new BlockHeader(1,
                    blockchain.getTopBlock().header.getHash(),
                    MerkleTreeUtils.getMerkleRootFromSerTxList(transactions.stream().map(t->t.serialize()).collect(Collectors.toList())),
                    DifficultyAdjustmentRedux.getNextBlockBits(blockchain));

            this.header.time = (System.currentTimeMillis() / 1000);
            return this;
        }

        public Builder generateHeaderWithGivenPrevBlock(Blockchain blockchain, byte[] prevBlockHash) {
            List<Transaction> transactionsPlusCoinBase = new ArrayList<>();
            transactionsPlusCoinBase.addAll(transactions);
            transactionsPlusCoinBase.add(coinbase);

            this.header = new BlockHeader(1,
                    prevBlockHash,
                    MerkleTreeUtils.getMerkleRootFromSerTxList(transactions.stream().map(t->t.serialize()).collect(Collectors.toList())),
                    DifficultyAdjustmentRedux.getNextBlockBits(blockchain));

            this.header.time = 1541423224;
            return this;
        }

        public Block end() {
            int size = 4 +
                    header.serialize().array().length +
                    8 +
                    transactions.stream().mapToInt(t->t.serialize().length).sum() +
                    coinbase.serialize().length;

            return new Block(size, header, transactionCounter, transactions, coinbase);
        }
    }
}
