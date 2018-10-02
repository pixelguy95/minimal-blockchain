package security;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

public class ECSignatureUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSigningAndVerifying() {
        byte[] plain = "Shit on a dick".getBytes();
        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(plain, kp.getPrivate());

        assertTrue(ECSignatureUtils.verify(signature, plain, kp.getPublic()));
    }

    @Test
    public void testSigningAndVerifyingWrongMessage() {
        byte[] plain = "Shit on a dick".getBytes();
        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(plain, kp.getPrivate());

        assertFalse(ECSignatureUtils.verify(signature, "I AM WRONG".getBytes(), kp.getPublic()));
    }

    @Test
    public void testSigningAndVerifyingWithWrongKey() {
        byte[] plain = "Shit on a dick".getBytes();
        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(plain, kp.getPrivate());

        System.out.println(signature.length);
        KeyPair otherKeys = ECKeyManager.generateNewKeyPair();
        assertFalse(ECSignatureUtils.verify(signature, plain, otherKeys.getPublic()));
    }

}