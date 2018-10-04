package cores;

import apis.HandshakeAPI;
import com.google.gson.Gson;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import io.nayuki.bitcoin.crypto.Base58Check;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

public class Node {

    public static void main(String[] args) {
        port(30109);

        Gson gson = new Gson();
        String version = Node.class.getPackage().getImplementationVersion();

        get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ)", gson::toJson);
        get("/handshake/:port", HandshakeAPI::newConnection, gson::toJson);
    }


}