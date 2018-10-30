package utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MerkleTreeUtils {

    private MerkleTreeUtils(){ }

    public static byte[] getMerkleRootFromSerTxList(List<byte[]> leaves){
        List<byte[]> l = new ArrayList<>();
        for(byte[] bytes : leaves){
            l.add(doubleSHA256Single(bytes));
        }
        return createMerkle(l);
    }

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
        return DigestUtils.sha256(DigestUtils.sha256(ByteBuffer.allocate(first.length + second.length).put(first).put(second).array()));
    }

    private static byte[] doubleSHA256Single(byte[] first) {
        return DigestUtils.sha256(DigestUtils.sha256(ByteBuffer.allocate(first.length).put(first).array()));
    }
}
