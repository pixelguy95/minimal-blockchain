package node;

import apis.BlockAPI;
import apis.HandshakeAPI;
import com.google.gson.Gson;
import node.domain.KnownNodesList;

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
        KnownNodesList.getKnownNodes();
    }
}