package apis.domain.requests;

import domain.block.Block;

public class NewBlockFoundRequest extends Request {
    public Block block;

    public NewBlockFoundRequest(Block block) {
        this.block = block;
    }
}
