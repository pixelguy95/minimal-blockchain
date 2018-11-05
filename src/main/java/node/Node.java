package node;

import apis.*;
import apis.domain.responses.GetAllBlockHashesResponse;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import apis.utils.validators.BlockValidator;
import apis.utils.validators.TransactionValidator;
import com.google.gson.*;
import db.DBHolder;
import domain.block.Block;
import io.nayuki.bitcoin.crypto.Ripemd160;
import node.tasks.BlockSync;
import node.tasks.Miner;
import node.tasks.NetworkSetup;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.digest.DigestUtils;
import org.restlet.resource.ResourceException;
import spark.Service;
import utils.DifficultyAdjustmentRedux;
import utils.RESTUtils;

public class Node {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        new Node(args);
    }

    private Service http;

    public AtomicBoolean isRunning;
    public Config config;

    private DBHolder dbs;
    private DebugAPI debugAPI;
    private TransactionAPI transactionAPI;
    private HandshakeAPI handshakeAPI;
    private BlockAPI blockAPI;
    private UTXOAPI utxoAPI;

    public TransactionPool transactionPool;
    public KnownNodesList knownNodesList;
    public Blockchain blockchain;
    public UTXO utxo;

    public Node(String[] args) {

        isRunning = new AtomicBoolean(true);
        config = new Config(args);

        System.out.println(DifficultyAdjustmentRedux.toTarget(0x1d06810e).toString(16));

        dbs = new DBHolder(config.dbFolder);
        if(config.isInitial) {
            initialNode(config);
        }

        transactionPool = new TransactionPool(dbs.getTransactionDB());
        knownNodesList = new KnownNodesList(dbs.getMetaDB());
        blockchain = new Blockchain(dbs.getBlockDB(), dbs.getBlockHeaderDB(), dbs.getMetaDB(), config);
        utxo = new UTXO(dbs.getUtxoDB());

        TransactionValidator transactionValidator = new TransactionValidator(utxo, blockchain, transactionPool);
        BlockValidator blockValidator = new BlockValidator(utxo, blockchain, transactionPool, transactionValidator, config);

        transactionAPI = new TransactionAPI(transactionPool, blockchain, knownNodesList, transactionValidator, config);
        debugAPI = new DebugAPI(transactionPool);
        handshakeAPI = new HandshakeAPI(knownNodesList);
        blockAPI = new BlockAPI(blockchain, utxo, transactionPool, blockValidator, transactionValidator, knownNodesList, config);
        utxoAPI = new UTXOAPI(utxo);

        if(!config.isInitial) {

            if(!RESTUtils.exists(config.initialConnection)) {
                System.err.println("INITIAL NODE WAS NOT FOUND " + config.initialConnection.ip + ":" + config.initialConnection.port);
                isRunning.set(false);
                return;
            }

            knownNodesList.addNode(config.initialConnection);
            new NetworkSetup(new AtomicBoolean(true), config, knownNodesList, blockchain).run();
            if(!new BlockSync(knownNodesList, blockchain, utxo, transactionPool, config, blockValidator, transactionValidator).sync()) {
                isRunning.set(false);
                return;
            }
        }


        setUpEndPoints(config);

        startTasks();
    }

    private void startTasks() {
        if(config.isMiningNode)
            new Thread(new Miner(new AtomicBoolean(true), blockchain, transactionPool, utxo, config)).start();
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
            http.get("/all", blockAPI::getAllBlockHashes, gson::toJson);
            http.get("/from/:height", blockAPI::getAllBlockHashesFromHeight, gson::toJson);
            http.get("/retransmission/:blockhash", blockAPI::retransmittedBlock, gson::toJson);
            http.get("/:blockhash", blockAPI::getBlock, gson::toJson);
            http.post("", blockAPI::newBlockFound, gson::toJson);
        });

        /*Transactions*/
        http.path("/transaction", () -> {
            http.post("", transactionAPI::newTransaction, gson::toJson);
            http.get("/:txid", transactionAPI::fetchTransaction, gson::toJson);
            http.get("/onblockchain/:txid", transactionAPI::fetchTransactionFromChain, gson::toJson);
            http.get("/retransmission/:txid", transactionAPI::retransmittedTransaction, gson::toJson);
        });

        /*UTXO*/
        http.path("/utxo", () -> {
            http.get("/:pubkey", utxoAPI::fetchUTXOByAddress, gson::toJson);
            http.get("/ids/:pubkey", utxoAPI::fetchUTXOIDsByAddress, gson::toJson);
            http.get("/:txid/:index", utxoAPI::fetchUTXO, gson::toJson);
        });

        /*Debug*/
        http.path("/debug", () -> {
            http.get("/tx-pool", debugAPI::getEntireTransactionPool, gson::toJson);
            http.get("/tx-pool-ids", debugAPI::getEntireTransactionPoolIDs, gson::toJson);
        });
    }

    public void kill() throws IOException {

        knownNodesList.getKnownNodes().stream().forEach(host -> {
            try {
                RESTUtils.get(host, "addr/leave", GetAllBlockHashesResponse.class, Arrays.asList(String.valueOf(config.port)));
            } catch (ResourceException e) {
            }
        });

        http.stop();
        dbs.closeAll();
    }

    public void destroyPersistantData() {
        dbs.destroy(config.dbFolder);
    }
}