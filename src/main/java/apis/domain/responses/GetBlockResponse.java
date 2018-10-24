package apis.domain.responses;

import domain.block.Block;

public class GetBlockResponse extends Response {

    public Block block;

    public GetBlockResponse(Block block) {
        this.block = block;
    }
}
