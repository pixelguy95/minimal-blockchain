package domain.transaction;

import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.security.KeyPair;

import static org.junit.Assert.*;

public class TransactionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSerialization() throws Exception {

        byte[] fakeTransactionHash = "Make this proper later".getBytes();

        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(fakeTransactionHash, kp.getPrivate());
        byte[] publicKey = kp.getPublic().getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        //Transaction transaction = new Transaction(1, 0, 1, 1, )
    }
}