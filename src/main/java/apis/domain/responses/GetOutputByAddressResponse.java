package apis.domain.responses;

import domain.transaction.Output;

import java.util.List;

public class GetOutputByAddressResponse extends Response{
    public List<Output> outputs;

    public GetOutputByAddressResponse(List<Output> outputs) {
        this.outputs = outputs;
    }
}
