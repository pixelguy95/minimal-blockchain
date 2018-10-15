package node;

import apis.BlockAPI;
import apis.HandshakeAPI;
import com.google.gson.Gson;
import db.DBSingletons;

import static spark.Spark.*;

public class Node {

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        Config.parse(args);

        if(Config.isInitial) {
            initial();
        } else {

        }

        setUpEndPoints();
    }

    public static void initial() {
        DBSingletons.destroy(Config.dbFolder);
    }

    public static void setUpEndPoints() {
        port(Config.port);
        Gson gson = new Gson();
        String version = Node.class.getPackage().getImplementationVersion();

        /*Network handling*/
        get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ)", gson::toJson);
        post("/addr/:port", HandshakeAPI::addr, gson::toJson);
        get("/handshake/:port", HandshakeAPI::newConnection, gson::toJson);
        get("/leave/:port", HandshakeAPI::leave, gson::toJson);

        /*Blocks*/
        get("/blockheight", BlockAPI::getCurrentBlockHeight, gson::toJson);
        get("/block/:id", BlockAPI::getBlock, gson::toJson);
        post("/new-block", BlockAPI::newBlockFound, gson::toJson);

        /*Transactions*/
        /*new-transaction*/
        /*get-transaction*/
        /*get-utxo*/
    }
}