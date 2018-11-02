import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import security.ECKeyManager;
import utils.DifficultyAdjustment;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Base64;

import static org.junit.Assert.assertNotEquals;

public class MetaTest {

    @Test
    public void metaTest() {
        assertNotEquals(2+2, 5);
    }

    @Test
    public void testRIPMD160HashSizes() {

        for(int i = 0; i < 100; i++) {
            KeyPair pair = ECKeyManager.generateNewKeyPair();
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(Ripemd160.getHash(DigestUtils.sha256(pair.getPublic().getEncoded()))));
            System.out.println(Ripemd160.getHash(DigestUtils.sha256(pair.getPublic().getEncoded())).length);
        }
    }

    @Test
    public void testModulusPEMDAS() {
        System.out.println(100 + 1 % 10);
    }

    @Test
    public void blockReward() {
        System.out.println((long)(Math.pow(2.0, Math.floor(210_000 / 210_000))));

    }
}