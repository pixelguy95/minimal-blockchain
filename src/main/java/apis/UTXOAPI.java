package apis;

import apis.domain.responses.GetOutputByAddressResponse;
import apis.domain.responses.GetOutputIDsByAddressResponse;
import apis.domain.responses.GetOutputResponse;
import apis.static_structures.UTXO;
import domain.transaction.Output;
import domain.utxo.UTXOIdentifier;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import script.OpCodes;
import script.ScriptExecutor;
import spark.Request;
import spark.Response;

import java.nio.ByteBuffer;
import java.util.*;

public class UTXOAPI {

    private UTXO utxo;

    public UTXOAPI(UTXO utxo) {
        this.utxo = utxo;
    }

    public GetOutputResponse fetchUTXO(Request request, Response response) {
        byte[] txid = Base64.getUrlDecoder().decode(request.params("txid"));
        int index = Integer.parseInt(request.params("index"));

        System.out.println(request.params("txid") + " " + request.params("index"));
        System.out.println(utxo.getAll().size());

        if(utxo.has(new UTXOIdentifier(txid, index))) {
            return new GetOutputResponse(utxo.get(new UTXOIdentifier(txid, index)));
        }

        return (GetOutputResponse) new GetOutputResponse(null).setError("No such output");
    }

    /**
     * This is unmaintainable as it is, if all the outputs exceeds the memory limit the entire node will crash.
     * Will work as a temporary solution however.
     * @param request
     * @param response
     * @return
     */
    public GetOutputByAddressResponse fetchUTXOByAddress(Request request, Response response) {

        List<Output> matches = new ArrayList<>();
        byte[] ripmd160Address = Base64.getUrlDecoder().decode(request.params("pubkey"));
        Map<UTXOIdentifier, Output> all = utxo.getAll();

        all.values().stream().forEach(output->{
            ByteBuffer script = ByteBuffer.wrap(output.scriptPubKey);

            if(isP2PKH(script)) {
                //Removes trivial op codes for writing to the stack.
                script.get();
                script.getInt();

                byte[] pubAddressFromScript = new byte[20];
                script.get(pubAddressFromScript, 0, 20);

                if(ByteBuffer.wrap(pubAddressFromScript).equals(ByteBuffer.wrap(ripmd160Address))) {
                    matches.add(output);
                }
            }
        });

        return new GetOutputByAddressResponse(matches);
    }

    public GetOutputIDsByAddressResponse fetchUTXOIDsByAddress(Request request, Response response) {

        List<UTXOIdentifier> matches = new ArrayList<>();
        byte[] ripmd160Address = Base64.getUrlDecoder().decode(request.params("pubkey"));
        Map<UTXOIdentifier, Output> all = utxo.getAll();

        all.entrySet().forEach(entry->{
            ByteBuffer script = ByteBuffer.wrap(entry.getValue().scriptPubKey);

            if(isP2PKH(script)) {
                //Removes trivial op codes for writing to the stack.
                script.get();
                script.getInt();

                byte[] pubAddressFromScript = new byte[20];
                script.get(pubAddressFromScript, 0, 20);

                if(ByteBuffer.wrap(pubAddressFromScript).equals(ByteBuffer.wrap(ripmd160Address))) {
                    matches.add(entry.getKey());
                }
            }
        });

        return new GetOutputIDsByAddressResponse(matches);
    }

    private boolean isP2PKH(ByteBuffer script) {
        return ScriptExecutor.unsignedToBytes(script.get()) == OpCodes.OP_DUP && ScriptExecutor.unsignedToBytes(script.get()) == OpCodes.OP_HASH160;
    }
}
