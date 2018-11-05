package apis.domain.responses;

import domain.utxo.UTXOIdentifier;

import java.util.List;

public class GetOutputIDsByAddressResponse extends Response {
    public List<UTXOIdentifier> ids;

    public GetOutputIDsByAddressResponse(List<UTXOIdentifier> ids) {
        this.ids = ids;
    }
}
