package domain.block;

import domain.transaction.CoinbaseTransaction;
import domain.transaction.Output;
import domain.transaction.Transaction;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;
import script.ScriptBuilder;
import security.ECKeyManager;
import utils.MerkeleTreeUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Serializable {
    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;
    public CoinbaseTransaction coinbase;

    public Block(List<Transaction> transactions, byte[] prevBlockHash, PublicKey pub) {
        this.transactions = transactions;
        this.transactionCounter = transactions.size();

        generateCoinBaseTransaction(pub);
        byte[] merkeleTreeRoot = generateMerkeleRoot();

        header = new BlockHeader(1, prevBlockHash, merkeleTreeRoot, BigInteger.ZERO);
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
     * WARNING: This might be completly wrong, dont be afraid to change, make sure to run all tests after however
     * @return
     */
    public byte[] generateMerkeleRoot() {
        List<byte[]> transactionBytes = transactions.stream().map(t->t.serialize()).collect(Collectors.toList());
        transactionBytes.add(coinbase.serialize());
        return MerkeleTreeUtils.createMerkle(transactionBytes);
    }

    public boolean equals(Object o) {
        Block otherBlock = (Block)o;

        return (ByteBuffer.wrap(this.header.getHash()).equals(ByteBuffer.wrap(otherBlock.header.getHash()))) &&
                this.blockSize == otherBlock.blockSize &&
                this.transactionCounter == otherBlock.transactionCounter &&
                this.transactions.equals(otherBlock.transactions) &&
                this.coinbase.equals(otherBlock.coinbase);
    }

    public static Block generateGenesisBlock() {
        PublicKey pub = ECKeyManager.bytesToPublicKey(Base64.getUrlDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAECibpSuVLXUv-dNRPpDJ7H416s8g0e3uqGMkTL23OOMewtkeOPH8GgByAu7-acla3bmORGH5GadbUEjBk9YE79A"));
        return new Block(Arrays.asList(), DigestUtils.sha256("GENESIS BLOCK"), pub);
    }
}
