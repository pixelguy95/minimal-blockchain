package domain.transaction;

import java.io.BufferedReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Output {

    public long amount;
    public long scriptPubKeyLength;
    public byte[] scriptPubKey;

    public Output(long amount, long scriptPubKeyLength, byte[] scriptPubKey) {
        this.amount = amount;
        this.scriptPubKeyLength = scriptPubKeyLength;
        this.scriptPubKey = scriptPubKey;
    }

    public byte[] serialize() {
        ByteBuffer bb = ByteBuffer.allocate((int) (Long.BYTES + Long.BYTES + scriptPubKeyLength));
        bb.putLong(amount);
        bb.putLong(scriptPubKeyLength);
        bb.put(scriptPubKey);

        return bb.array();
    }

    public static Output fromBytes(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        long amount = bb.getLong();
        long scriptPubKeyLength = bb.getLong();
        byte[] scriptPubKey = new byte[(int) scriptPubKeyLength];
        bb.get(scriptPubKey, 0, (int) scriptPubKeyLength);

        return new Output(amount, scriptPubKeyLength, scriptPubKey);
    }
}
