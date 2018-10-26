package apis.static_structures;

import db.DBHolder;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.utxo.UTXOIdentifier;
import node.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

import static org.junit.Assert.*;

public class UTXOTest {

    private UTXO utxo;
    private DBHolder dbs;
    private KeyPair pair1;
    private KeyPair pair2;
    private KeyPair pair3;

    @Before
    public void setUp() throws Exception {
        dbs = new DBHolder(".test-persist");
        Config config = new Config(new String[]{});
        config.allowOrphanBlocks = true;
        utxo = new UTXO(dbs.getUtxoDB());

        pair1 = ECKeyManager.generateNewKeyPair();
        pair2 = ECKeyManager.generateNewKeyPair();
        pair3 = ECKeyManager.generateNewKeyPair();
    }

    @After
    public void tearDown() throws Exception {
        dbs.destroy(".test-persist");
    }

    @Test
    public void putAndGetAndRemoveAndHas() {

        Transaction t1 = Transaction.makeFakeTransaction(pair1.getPrivate(), pair1.getPublic());
        Transaction t2 = Transaction.makeFakeTransaction(pair2.getPrivate(), pair2.getPublic());
        Transaction t3 = Transaction.makeFakeTransaction(pair3.getPrivate(), pair3.getPublic());

        UTXOIdentifier id1 = new UTXOIdentifier(t1.fullHash(), 0);
        UTXOIdentifier id2 = new UTXOIdentifier(t2.fullHash(), 0);
        UTXOIdentifier id3 = new UTXOIdentifier(t3.fullHash(), 0);

        Output output1 = t1.outputs.get(0);
        Output output2 = t2.outputs.get(0);
        Output output3 = t3.outputs.get(0);

        utxo.put(id1, output1);
        assertTrue(utxo.has(id1));
        assertFalse(utxo.has(id2));
        assertFalse(utxo.has(id3));

        utxo.put(id2, output2);
        utxo.put(id3, output3);
        assertTrue(utxo.has(id1));
        assertTrue(utxo.has(id2));
        assertTrue(utxo.has(id3));

        utxo.remove(id1);
        assertFalse(utxo.has(id1));
        assertTrue(utxo.has(id2));
        assertTrue(utxo.has(id3));
    }

    @Test
    public void getAll() {
        Transaction t1 = Transaction.makeFakeTransaction(pair1.getPrivate(), pair1.getPublic());
        Transaction t2 = Transaction.makeFakeTransaction(pair2.getPrivate(), pair2.getPublic());
        Transaction t3 = Transaction.makeFakeTransaction(pair3.getPrivate(), pair3.getPublic());

        UTXOIdentifier id1 = new UTXOIdentifier(t1.fullHash(), 0);
        UTXOIdentifier id2 = new UTXOIdentifier(t2.fullHash(), 0);
        UTXOIdentifier id3 = new UTXOIdentifier(t3.fullHash(), 0);

        Output output1 = t1.outputs.get(0);
        Output output2 = t2.outputs.get(0);
        Output output3 = t3.outputs.get(0);

        utxo.put(id1, output1);
        utxo.put(id2, output2);
        utxo.put(id3, output3);

        Map<UTXOIdentifier, Output> allUTXOs = utxo.getAll();
        assertTrue(allUTXOs.containsKey(id1));
        assertTrue(allUTXOs.containsKey(id2));
        assertTrue(allUTXOs.containsKey(id3));

        assertEquals(output1, allUTXOs.get(id1));
        assertEquals(output2, allUTXOs.get(id2));
        assertEquals(output3, allUTXOs.get(id3));
    }
}