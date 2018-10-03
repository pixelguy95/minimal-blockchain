package block;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MerkleTree {

    private MessageDigest md;

    MerkleTree() throws NoSuchAlgorithmException {
        this.md = MessageDigest.getInstance("SHA-256");
    }

    public byte[] createMerkle(List<byte[]> hashes){

        if(hashes.isEmpty()){
            return null;
        }

        while (hashes.size() > 1){

            // If list is odd, duplicate last hash so it's even.
            if(hashes.size() % 2 == 1){
                hashes.add(hashes.get(hashes.size()-1));
            }

            //Hash the two first hashes and put back the result element last.
            int listSize = hashes.size();
            for(int i = 0; i < listSize / 2; i++) {
                hashes.add(doubleSHA256(hashes.remove(0), hashes.remove(0)));
            }
        }

        return hashes.get(0);
    }

    private byte[] doubleSHA256(byte[] first, byte[] second) {
        return md.digest(md.digest(ByteBuffer.allocate(64).put(first).put(second).array()));
    }
}