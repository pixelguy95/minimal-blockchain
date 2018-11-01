package domain.block;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class BlockHeader implements Serializable {
    public int version;
    public byte[] prevBlockHash;
    public byte[] merkleRoot;
    public long time = 0; // Wtf 8 bytes? Yes, 4 bytes is outdated and will not work after January 19, 2038. wtf were you thinking satoshi nakamoto?
    public byte[] difficultyBits; // Not to be confused with difficulty = genesis_target / current_target
    private long nonce = 0;

    public BlockHeader(int version, byte[] prevBlockHash, byte[] merkleRoot, byte[] difficultyBits) {
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkleRoot = merkleRoot;
        this.difficultyBits = difficultyBits;
    }

    /**
     * Returns a serialized version of the blockheader with the nonce left blank. Add this manually when hashing.
     * Ask CJ if this doesn't make sense.
     * @return
     */
    public ByteBuffer serialize() {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + 32 + 32 + Long.BYTES + 64 + Long.BYTES);
        bb.putInt(version);
        bb.put(prevBlockHash);
        bb.put(merkleRoot);
        bb.putLong(time);
        bb.put(difficultyBits);

        return bb;
    }

    public void incrementNonce() {
        this.nonce = this.nonce++;
    }

    public byte[] getHash() {
        return DigestUtils.sha256(serialize().putLong(nonce).array());
    }
}
