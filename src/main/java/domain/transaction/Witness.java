package domain.transaction;

import java.nio.ByteBuffer;

/**
 * Might be cut from the final implementation, still left in the transaction however.
 */
public class Witness {

    public byte[] serialize() {
        return new byte[0];
    }

    public static Witness fromBytes(byte[] data) {
        return null;
    }

}
