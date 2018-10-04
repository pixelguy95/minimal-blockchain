package domain.transaction;

import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TransactionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSerialization() throws Exception {

        byte[] fakePartial = DigestUtils.sha256("This will be the partial hash".getBytes());
        byte[] fakeTransactionHash = DigestUtils.sha256("This will be the full hash".getBytes());

        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(fakePartial, kp.getPrivate());
        byte[] publicKey = kp.getPublic().getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        Input input = new Input(fakeTransactionHash, 0, scriptSig.length, scriptSig, 0xFFFFFFFF);
        List<Input> inputs = Arrays.asList(input);

        Output output = new Output(10, scriptPubKey.length, scriptPubKey);
        List<Output> outputs = Arrays.asList(output);

        Transaction transaction1 = new Transaction(1, (short) 0, 1, 1, inputs, outputs, null, 0xFFFFFFFF);

        byte[] serial = transaction1.serialize();
        System.out.println("Size of transaction in bytes " + serial.length);
        Transaction transaction2 = Transaction.fromBytes(serial);

        assertEquals(transaction1.version, transaction2.version);
        assertEquals(transaction1.flag, transaction2.flag);
        assertEquals(transaction1.inCounter, transaction2.inCounter);
        assertEquals(transaction1.outCounter, transaction2.outCounter);

        assertTrue(transaction1.outputs.size() == transaction2.outputs.size());
        assertArrayEquals(transaction1.outputs.get(0).scriptPubKey, transaction2.outputs.get(0).scriptPubKey);

        assertTrue(transaction1.inputs.size() == transaction2.inputs.size());
        assertArrayEquals(transaction1.inputs.get(0).scriptSig, transaction2.inputs.get(0).scriptSig);
    }
}