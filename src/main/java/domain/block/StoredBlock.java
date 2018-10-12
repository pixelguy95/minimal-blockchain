package domain.block;

import domain.transaction.Transaction;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class StoredBlock implements Serializable {
    public long height;
    public BlockHeader blockHeader;

    public StoredBlock(long height, BlockHeader blockHeader) {
        this.height = height;
        this.blockHeader = blockHeader;
    }
}
