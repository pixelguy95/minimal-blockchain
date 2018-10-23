package node;

import apis.*;
import com.google.gson.*;
import db.DBSingletons;
import node.tasks.NetworkSetup;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import spark.Service;
import spark.Spark;

public class Node {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        new Node(args);
    }

    public Node(String[] args) {
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
        Service http = Service.ignite();
        http.port(Config.port);

        Gson gson = SpecialJSONSerializer.getInstance();

        String version = Node.class.getPackage().getImplementationVersion();

        /*Network handling*/
        http.get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ/Debug mode)", gson::toJson);
        http.post("/handshake", HandshakeAPI::handShake, gson::toJson);

        http.path("/addr", () -> {
            http.post("", HandshakeAPI::addr, gson::toJson);
            http.get("", HandshakeAPI::getAddresses, gson::toJson);
            http.get("/leave/:port", HandshakeAPI::leave, gson::toJson);
        });

        /*Blocks*/
        http.path("/block", () -> {
            http.get("/height", BlockAPI::getCurrentBlockHeight, gson::toJson);
            http.get("/:id", BlockAPI::getBlock, gson::toJson);
            http.post("", BlockAPI::newBlockFound, gson::toJson);
        });


        /*Transactions*/
        http.path("/transaction", () -> {
            http.post("", TransactionAPI::newTransaction, gson::toJson);
            http.get("/:txid", TransactionAPI::fetchTransaction, gson::toJson);
            http.get("/retransmission/:txid", TransactionAPI::retransmittedTransaction, gson::toJson);
        });

        http.path("/utxo", () -> {
            http.get("/:pubkey", UTXOAPI::fetchUTXO, gson::toJson);
        });
        /*get-utxo*/

        /*Debug*/
        http.path("/debug", () -> {
            http.get("/tx-pool", DebugAPI::getEntireTransactionPool, gson::toJson);
            http.get("/tx-pool-ids", DebugAPI::getEntireTransactionPoolIDs, gson::toJson);
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