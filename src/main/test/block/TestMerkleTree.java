package block;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class TestMerkleTree {

    @Test
    public void testCreateMerkleTree() {

        try {
            MerkleTree mt = new MerkleTree();
            Random r = new Random();

            byte[] first = new byte[32];
            byte[] second = new byte[32];
            byte[] third = new byte[32];
            byte[] forth = new byte[32];

            r.nextBytes(first);
            r.nextBytes(second);
            r.nextBytes(third);
            r.nextBytes(forth);

            byte[] shouldBe = doubleSHA256(doubleSHA256(first, second), doubleSHA256(third, forth));

            List<byte[]> l = new LinkedList<>();

            assertEquals(mt.createMerkle(l), shouldBe);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private byte[] doubleSHA256(byte[] first, byte[] second) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(md.digest(ByteBuffer.allocate(40).put(ByteBuffer.allocate(64).put(first).put(second)).array()));

    }

}
