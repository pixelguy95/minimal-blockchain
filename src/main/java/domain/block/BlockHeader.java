package domain.block;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BlockHeader implements Serializable {
    public int version;
    public byte[] prevBlockHash;
    public byte[] merkeleRoot;
    public long time = 0; // Wtf 8 bytes? Yes, 4 bytes is outdated and will not work after January 19, 2038. wtf were you thinking satoshi nakamoto?
    BigInteger target;
    private long nonce = 0;

    public BlockHeader(int version, byte[] prevBlockHash, byte[] merkeleRoot, BigInteger target) {
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkeleRoot = merkeleRoot;
        this.target = target;
    }

    /**
     * Returns a serialized version of the blockheader with the nonce left blank. Add this manually when hashing.
     * Ask CJ if this doesn't make sense.
     * @return
     */
    public ByteBuffer serialize() {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + 32 + 32 + Long.BYTES + 32 + Long.BYTES);
        bb.putInt(version);
        bb.put(prevBlockHash);
        bb.put(merkeleRoot);
        bb.putLong(time);
        bb.put(target.toByteArray());

        return bb;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public byte[] getHash() {
        return DigestUtils.sha256(serialize().putLong(nonce).array());
    }
}
