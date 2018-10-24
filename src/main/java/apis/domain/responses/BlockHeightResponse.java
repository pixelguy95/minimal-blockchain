package apis.domain.responses;

import java.math.BigInteger;

public class BlockHeightResponse extends Response {
    public BigInteger height;

    public BlockHeightResponse(BigInteger height) {
        this.height = height;
    }
}
