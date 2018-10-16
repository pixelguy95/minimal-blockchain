package node;

import apis.BlockAPI;
import apis.HandshakeAPI;
import apis.domain.HandshakeRequest;
import apis.domain.HandshakeResponse;
import apis.static_structures.KnownNodesList;
import com.google.gson.Gson;
import db.DBSingletons;
import node.tasks.NetworkSetup;
import utils.RESTUtils;

import java.util.Arrays;

import static spark.Spark.*;

public class Node {

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        Config.parse(args);

        if(Config.isInitial) {
            initialNode();
        } else {
            new NetworkSetup().run();
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
        get("/blockheight", BlockAPI::getCurrentBlockHeight, gson::toJson);
        get("/block/:id", BlockAPI::getBlock, gson::toJson);
        post("/new-block", BlockAPI::newBlockFound, gson::toJson);

        /*Transactions*/
        /*new-transaction*/
        /*get-transaction*/
        /*get-utxo*/
    }
}