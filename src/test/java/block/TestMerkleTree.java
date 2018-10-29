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
    public void testCreateMerkleTreeFullPair() {

        try {
            MerkleTree mt = new MerkleTree();
            Random r = new Random(System.currentTimeMillis());

            byte[] first = new byte[32];
            byte[] second = new byte[32];
            byte[] third = new byte[32];
            byte[] forth = new byte[32];

            r.nextBytes(first);
            r.nextBytes(second);
            r.nextBytes(third);
            r.nextBytes(forth);

            List<byte[]> l = new LinkedList<>();

            l.add(first.clone());
            l.add(second.clone());
            l.add(third.clone());
            l.add(forth.clone());

            byte[] shouldBe = doubleSHA256(doubleSHA256(first, second), doubleSHA256(third, forth));

            assertArrayEquals(mt.createMerkle(l), shouldBe);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateMerkleTreeIncompletePair() {

        try {
            MerkleTree mt = new MerkleTree();
            Random r = new Random(System.currentTimeMillis());

            byte[] first = new byte[32];
            byte[] second = new byte[32];
            byte[] third = new byte[32];

            r.nextBytes(first);
            r.nextBytes(second);
            r.nextBytes(third);

            List<byte[]> l = new LinkedList<>();

            l.add(first.clone());
            l.add(second.clone());
            l.add(third.clone());

            byte[] shouldBe = doubleSHA256(doubleSHA256(first, second), doubleSHA256(third, third));

            assertArrayEquals(mt.createMerkle(l), shouldBe);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOnlyOne() {
        try {
            MerkleTree mt = new MerkleTree();
            Random r = new Random(System.currentTimeMillis());

            byte[] first = new byte[32];

            r.nextBytes(first);

            List<byte[]> l = new LinkedList<>();

            l.add(first.clone());;

            assertArrayEquals(mt.createMerkle(l), first);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    private byte[] doubleSHA256(byte[] first, byte[] second) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(md.digest(ByteBuffer.allocate(64).put(first).put(second).array()));

    }


}
