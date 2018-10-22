package node;

import apis.*;
import com.google.gson.*;
import db.DBSingletons;
import node.tasks.NetworkSetup;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import static spark.Spark.*;

public class Node {

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        Config.parse(args);

        if(Config.isInitial) {
            initialNode();
        } else {
            new NetworkSetup(new AtomicBoolean(true)).run();
        }

        setUpEndPoints();
    }

    public static void initialNode() {
        DBSingletons.destroy(Config.dbFolder);
        DBSingletons.init(Config.dbFolder);
    }

    public static void setUpEndPoints() {
        port(Config.port);

        Gson gson = SpecialJSONSerializer.getInstance();

        String version = Node.class.getPackage().getImplementationVersion();

        /*Network handling*/
        get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ/Debug mode)", gson::toJson);
        post("/handshake", HandshakeAPI::handShake, gson::toJson);

        path("/addr", () -> {
            post("", HandshakeAPI::addr, gson::toJson);
            get("", HandshakeAPI::getAddresses, gson::toJson);
            get("/leave/:port", HandshakeAPI::leave, gson::toJson);
        });

        /*Blocks*/
        path("/block", () -> {
            get("/height", BlockAPI::getCurrentBlockHeight, gson::toJson);
            get("/:id", BlockAPI::getBlock, gson::toJson);
            post("", BlockAPI::newBlockFound, gson::toJson);
        });


        /*Transactions*/
        path("/transaction", () -> {
            post("", TransactionAPI::newTransaction, gson::toJson);
            get("/:txid", TransactionAPI::fetchTransaction, gson::toJson);
            get("/retransmission/:txid", TransactionAPI::retransmittedTransaction, gson::toJson);
        });

        path("/utxo", () -> {
           get("/:pubkey", UTXOAPI::fetchUTXO, gson::toJson);
        });
        /*get-utxo*/

        /*Debug*/
        path("/debug", () -> {
            get("/tx-pool", DebugAPI::getEntireTransactionPool, gson::toJson);
            get("/tx-pool-ids", DebugAPI::getEntireTransactionPoolIDs, gson::toJson);
        });
    }

    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            System.out.println(json.getAsString());
            return Base64.getUrlDecoder().decode(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getUrlEncoder().withoutPadding().encodeToString(src));
        }
    }
}