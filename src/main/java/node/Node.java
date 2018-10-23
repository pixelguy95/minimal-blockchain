package node;

import apis.*;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
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

    private Service http;

    public Config config;

    private DBSingletons dbs;
    private DebugAPI debugAPI;
    private TransactionAPI transactionAPI;
    private HandshakeAPI handshakeAPI;
    private BlockAPI blockAPI;

    public TransactionPool transactionPool;
    public KnownNodesList knownNodesList;
    public Blockchain blockchain;

    public Node(String[] args) {
        config = new Config(args);

        dbs = new DBSingletons(config.dbFolder);
        if(config.isInitial) {
            initialNode(config);
        }

        transactionPool = new TransactionPool(dbs.getTransactionDB());
        knownNodesList = new KnownNodesList(dbs.getMetaDB());
        blockchain = new Blockchain(dbs.getBlockDB(), dbs.getBlockHeaderDB(), dbs.getMetaDB());

        transactionAPI = new TransactionAPI(transactionPool, knownNodesList);
        debugAPI = new DebugAPI(transactionPool);
        handshakeAPI = new HandshakeAPI(knownNodesList);
        blockAPI = new BlockAPI(blockchain);

        if(!config.isInitial) {
            knownNodesList.addNode(config.initialConnection);
            new NetworkSetup(new AtomicBoolean(true), config, knownNodesList, blockchain).run();
        }

        setUpEndPoints(config);
    }

    public void initialNode(Config config) {
        dbs.destroy(config.dbFolder);
        dbs.restart(config.dbFolder);
    }

    public void setUpEndPoints(Config config) {
        http = Service.ignite();
        http.port(config.port);

        Gson gson = SpecialJSONSerializer.getInstance();

        String version = Node.class.getPackage().getImplementationVersion();

        /*Network handling*/
        http.get("/version", (req, res) -> version != null ? version : "Unknown (IntelliJ/Debug mode)", gson::toJson);
        http.post("/handshake", handshakeAPI::handShake, gson::toJson);

        http.path("/addr", () -> {
            http.post("", handshakeAPI::addr, gson::toJson);
            http.get("", handshakeAPI::getAddresses, gson::toJson);
            http.get("/leave/:port", handshakeAPI::leave, gson::toJson);
        });

        /*Blocks*/
        http.path("/block", () -> {
            http.get("/height", blockAPI::getCurrentBlockHeight, gson::toJson);
            http.get("/:id", blockAPI::getBlock, gson::toJson);
            http.post("", blockAPI::newBlockFound, gson::toJson);
        });


        /*Transactions*/
        http.path("/transaction", () -> {
            http.post("", transactionAPI::newTransaction, gson::toJson);
            http.get("/:txid", transactionAPI::fetchTransaction, gson::toJson);
            http.get("/retransmission/:txid", transactionAPI::retransmittedTransaction, gson::toJson);
        });

        http.path("/utxo", () -> {
            http.get("/:pubkey", UTXOAPI::fetchUTXO, gson::toJson);
        });
        /*get-utxo*/

        /*Debug*/
        http.path("/debug", () -> {
            http.get("/tx-pool", debugAPI::getEntireTransactionPool, gson::toJson);
            http.get("/tx-pool-ids", debugAPI::getEntireTransactionPoolIDs, gson::toJson);
        });
    }

    public void kill() {
        http.stop();
    }

    public void destroyPersistantData() {
        dbs.destroy(config.dbFolder);
    }
}