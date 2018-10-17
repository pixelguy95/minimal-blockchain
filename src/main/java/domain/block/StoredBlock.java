package domain.block;

import java.io.Serializable;
public class StoredBlock implements Serializable {
    public long height;
    public BlockHeader blockHeader;

    public StoredBlock(long height, BlockHeader blockHeader) {
        this.height = height;
        this.blockHeader = blockHeader;
    }
}
