package node;

import apis.BlockAPI;
import apis.DebugAPI;
import apis.HandshakeAPI;
import apis.TransactionAPI;
import com.google.gson.Gson;
import db.DBSingletons;
import node.tasks.NetworkSetup;

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
        Gson gson = new Gson();
        String version = Node.class.getPackage().getImplementationVersion();

        /*Network handling*/
        get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ)", gson::toJson);
        post("/addr", HandshakeAPI::addr, gson::toJson);
        get("/getaddr", HandshakeAPI::getAddresses, gson::toJson);
        post("/handshake", HandshakeAPI::handShake, gson::toJson);
        get("/leave/:port", HandshakeAPI::leave, gson::toJson);

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
        /*get-utxo*/

        /*Debug*/
        path("/debug", () -> {
            get("/tx-pool", DebugAPI::getEntireTransactionPool, gson::toJson);
            get("/tx-pool-ids", DebugAPI::getEntireTransactionPoolIDs, gson::toJson);
        });
    }
}