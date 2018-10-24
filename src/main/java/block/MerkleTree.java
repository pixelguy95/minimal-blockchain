package block;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Deprecated
/**
 * See MerkeleTreeUtils
 */
public class MerkleTree {

    private MessageDigest md;

    MerkleTree() throws NoSuchAlgorithmException {
        this.md = MessageDigest.getInstance("SHA-256");
    }

    public byte[] createMerkle(List<byte[]> leaves){

        if(leaves.isEmpty()){
            return null;
        }

        while (leaves.size() > 1){

            // If list is odd, duplicate last hash so it's even.
            if(leaves.size() % 2 == 1){
                leaves.add(leaves.get(leaves.size()-1));
            }

            //Hash the two first hashes and put back the result element last.
            int listSize = leaves.size();
            for(int i = 0; i < listSize / 2; i++) {
                leaves.add(doubleSHA256(leaves.remove(0), leaves.remove(0)));
            }
        }

        return leaves.get(0);
    }

    private byte[] doubleSHA256(byte[] first, byte[] second) {
        return md.digest(md.digest(ByteBuffer.allocate(64).put(first).put(second).array()));
    }
}