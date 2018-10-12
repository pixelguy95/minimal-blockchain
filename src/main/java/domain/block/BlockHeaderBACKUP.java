package domain.block;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;
import java.math.BigInteger;


/**
 * This is utter garbage, ignore for now
 */


































public class BlockHeaderBACKUP implements Serializable {
    public int version;
    public byte[] prevBlockHash;
    public byte[] merkeleRoot;
    public long time = 0; // Wtf 8 bytes? Yes, 4 bytes is outdated and will not work after January 19, 2038. wtf were you thinking satoshi nakamoto?
    BigInteger target;
    //Where is the nonce? Add it to the end of the serialization before you hash

    public BlockHeaderBACKUP(int version, byte[] prevBlockHash, byte[] merkeleRoot, BigInteger target) {
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkeleRoot = merkeleRoot;
        this.target = target;
    }

    /**
     * Right now this is only used when hashing, thus the return type can be a bytebuffer with extra space for the nonce
     * The way nonce overflows seems pointlessly complicated, so lets make nonce a long. FUCK SATOSHIS VISION
     * @return
     */
//    public ByteBuffer serialize() {
//        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + 32 + 32 + Long.BYTES + 32 + Long.BYTES);
//        bb.putInt(version);
//        bb.put(prevBlockHash);
//        bb.put(merkeleRoot);
//        bb.putLong(time);
//        //TODO Serialize the target and stuff
//
//        return bb;
//    }

    public byte[] getHash() {
        return DigestUtils.sha256(SerializationUtils.serialize(this));
    }
}
