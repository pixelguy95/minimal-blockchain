package domain.block;

import domain.transaction.Transaction;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;
import utils.MerkeleTreeUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Serializable {
    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;

    public Block(List<Transaction> transactions, byte[] prevBlockHash, BigInteger target) {
        this.transactions = transactions;
        this.transactionCounter = transactions.size();

        byte[] merkeleTreeRoot = MerkeleTreeUtils.createMerkle(transactions.stream().map(t->t.serialize()).collect(Collectors.toList()));
        header = new BlockHeader(1, prevBlockHash, merkeleTreeRoot, target);
    }
}
