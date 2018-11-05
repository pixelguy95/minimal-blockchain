package apis.utils.wrappers;

import apis.domain.Host;
import apis.domain.requests.NewBlockFoundRequest;
import apis.domain.responses.*;
import apis.static_structures.UTXO;
import domain.block.Block;
import domain.transaction.Output;
import domain.utxo.UTXOIdentifier;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import utils.RESTUtils;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

public class UTXORESTWrapper {

    public static GetOutputResponse getUTXO(Host host, UTXOIdentifier id) {
        String txid = Base64.getUrlEncoder().withoutPadding().encodeToString(id.txid);
        String index = String.valueOf(id.outputIndex);
        return RESTUtils.get(host, "utxo", GetOutputResponse.class, Arrays.asList(txid, index));
    }

    public static GetOutputByAddressResponse getUTXOByPubKey(Host host, PublicKey pub) {
        String pubkey = Base64.getUrlEncoder().withoutPadding().encodeToString(Ripemd160.getHash(DigestUtils.sha256(pub.getEncoded())));
        return RESTUtils.get(host, "utxo", GetOutputByAddressResponse.class, Arrays.asList(pubkey));
    }

    public static GetOutputIDsByAddressResponse getUTXOIDsByPubKey(Host host, PublicKey pub) {
        String pubkey = Base64.getUrlEncoder().withoutPadding().encodeToString(Ripemd160.getHash(DigestUtils.sha256(pub.getEncoded())));
        return RESTUtils.get(host, "utxo/ids", GetOutputIDsByAddressResponse.class, Arrays.asList(pubkey));
    }
}
