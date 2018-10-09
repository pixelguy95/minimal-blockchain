package cores;

import apis.BlockAPI;
import apis.HandshakeAPI;
import block.Block;
import com.google.gson.Gson;
import db.DBSingleton;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import io.nayuki.bitcoin.crypto.Base58Check;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static spark.Spark.*;

public class Node {



    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        port(30109);

        Gson gson = new Gson();
        String version = Node.class.getPackage().getImplementationVersion();

        /*Network handling*/
        get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ)", gson::toJson);
        get("/handshake/:port", HandshakeAPI::newConnection, gson::toJson);
        get("/leave/:port", HandshakeAPI::leave, gson::toJson);

        /*Blocks*/
        get("/blockheight", BlockAPI::getCurrentBlockHeight, gson::toJson);
        get("/block/:id", BlockAPI::getBlock, gson::toJson);
        post("/new-block", BlockAPI::newBlockFound, gson::toJson);

        DB db = null;
        try {
            db = DBSingleton.getInstance();
            db.put(bytes("Tampa"), bytes("rocks"));
            String value = asString(db.get(bytes("Tampa")));
            System.out.println(value);
            db.delete(bytes("Tampa"));

            value = asString(db.get(bytes("Tampa")));
            System.out.println(value);
        } finally {
            try {
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
