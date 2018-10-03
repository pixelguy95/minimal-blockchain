package script;

import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.math.BigInteger;
import java.security.KeyPair;

import static org.junit.Assert.assertTrue;

public class ScriptBuilderTest {

    ScriptBuilder sb;
    @Before
    public void setUp() throws Exception {
        sb = ScriptBuilder.newScript();
    }

    @Test
    public void buildAdditionScript() {
        sb.writeIntToStack(3);
        sb.writeIntToStack(4);
        sb.add();
        sb.writeIntToStack(7);
        sb.equals();

        System.out.println("Script");
        System.out.println(new BigInteger(sb.end()).toString(16));

        assertTrue(ScriptExecutor.executeWithCheckSig(sb.end(), null));
    }

    @Test
    public void buildP2PKSAndVerify() {
        byte[] fakeTransactionHash = "Make this proper later".getBytes();

        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(fakeTransactionHash, kp.getPrivate());
        byte[] publicKey = kp.getPublic().getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        byte[] combinedScript = ArrayUtils.addAll(scriptSig, scriptPubKey);
        System.out.println(combinedScript.length);

        System.out.println(new BigInteger(scriptSig).toString(16));
        System.out.println(new BigInteger(scriptPubKey).toString(16));
        System.out.println(new BigInteger(combinedScript).toString(16));

        assertTrue(ScriptExecutor.executeWithCheckSig(combinedScript, fakeTransactionHash));
    }
}