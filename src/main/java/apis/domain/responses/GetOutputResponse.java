package apis.domain.responses;

import domain.transaction.Output;

public class GetOutputResponse extends Response{
    public Output output;

    public GetOutputResponse(Output output) {
        this.output = output;
    }
}
