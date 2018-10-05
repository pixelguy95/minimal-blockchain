package domain.block;

import domain.transaction.Transaction;
import utils.MerkeleTreeUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Block {
    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;

    public Block(List<Transaction> transactions, byte[] prevBlockHash, BigDecimal target) {
        this.transactions = transactions;
        this.transactionCounter = transactions.size();

        byte[] merkeleTreeRoot = MerkeleTreeUtils.createMerkle(transactions.stream().map(t->t.serialize()).collect(Collectors.toList()));
        header = new BlockHeader(1, prevBlockHash, merkeleTreeRoot, target);
    }
}
