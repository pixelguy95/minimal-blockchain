package utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.List;

public class MerkeleTreeUtils {

    public static byte[] createMerkle(List<byte[]> leaves){

        if(leaves.isEmpty()){
            return DigestUtils.sha256("place holder");
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

    private static byte[] doubleSHA256(byte[] first, byte[] second) {
        return DigestUtils.sha256(DigestUtils.sha256(ByteBuffer.allocate(64).put(first).put(second).array()));
    }
}
