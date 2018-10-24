package domain.block;

import domain.transaction.CoinbaseTransaction;
import domain.transaction.Output;
import domain.transaction.Transaction;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;
import utils.MerkeleTreeUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Serializable {
    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;
    public CoinbaseTransaction coinbase;

    public Block(List<Transaction> transactions, byte[] prevBlockHash, BigInteger target) {
        this.transactions = transactions;
        this.transactionCounter = transactions.size();

        coinbase = new CoinbaseTransaction(1, (short) 0, new Output(100, 0, new byte[]{}), null, 0xFFFFFFFF);

        byte[] merkeleTreeRoot = MerkeleTreeUtils.createMerkle(transactions.stream().map(t->t.serialize()).collect(Collectors.toList()));
        header = new BlockHeader(1, prevBlockHash, merkeleTreeRoot, target);
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
        return new Block(Arrays.asList(), DigestUtils.sha256("GENESIS BLOCK"), BigInteger.TEN);
    }
}
