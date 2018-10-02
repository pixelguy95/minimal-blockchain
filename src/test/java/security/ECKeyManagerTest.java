package security;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

public class ECKeyManagerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void generateKeyPair() {
        KeyPair pair = ECKeyManager.generateNewKeyPair();

        assertEquals(pair.getPrivate().getEncoded().length, 144);
        assertEquals(pair.getPublic().getEncoded().length, 88);
    }

    // TODO: More tests, saving and loading etc
}