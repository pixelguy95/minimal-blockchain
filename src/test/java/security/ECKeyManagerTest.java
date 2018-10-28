package security;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
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

    @Test
    public void savingAndLoading() {
        KeyPair pair = ECKeyManager.generateNewKeyPair();

        ECKeyManager.writePairToFile(pair, ".key.pair");
        File f = new File(".key.pair");
        assertTrue(f.exists() && f.isFile());

        KeyPair loadedPair = ECKeyManager.loadPairFromFile(".key.pair");

        assertEquals(loadedPair.getPublic(), pair.getPublic());
        assertEquals(loadedPair.getPrivate(), pair.getPrivate());
    }

    // TODO: More tests, saving and loading etc
}