package domain.block;

import apis.static_structures.Blockchain;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Serializable , Validatable {

    public static final long INITIAL_REWARD = 5000000 * 1000;

    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;
    public CoinbaseTransaction coinbase;

    @Deprecated
    public Block(List<Transaction> transactions, byte[] prevBlockHash, PublicKey pub) {
        this.transactions = transactions;
        this.transactionCounter = transactions.size();

        generateCoinBaseTransaction(pub);
        byte[] merkeleTreeRoot = generateMerkeleRoot();

        header = new BlockHeader(1, prevBlockHash, merkeleTreeRoot, 0);
    }

    public Block(int blockSize, BlockHeader header, long transactionCounter, List<Transaction> transactions, CoinbaseTransaction coinbase) {
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

        Block genesis = new BlockBuilder()
                .putTransactions(Arrays.asList())
                .generateCoinBaseWithoutFees(pub, 0)
                .generateHeaderWithGivenPrevBlock(blockchain, DigestUtils.sha256("GENESIS BLOCK")).end();

        return genesis;
    }
}
