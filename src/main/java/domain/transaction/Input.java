package domain.transaction;

import domain.utxo.UTXOIdentifier;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class Input implements Serializable {

    public byte[] transactionHash;
    public int outputIndex;
    public long scriptSigSize;
    public byte[] scriptSig;
    public int sequence;

    public Input(byte[] transactionHash, int outputIndex, long scriptSigSize, byte[] scriptSig, int sequence) {
        this.transactionHash = transactionHash;
        this.outputIndex = outputIndex;
        this.scriptSigSize = scriptSigSize;
        this.scriptSig = scriptSig;
        this.sequence = sequence;
    }

    public byte[] serialize() {
        ByteBuffer bb = ByteBuffer.allocate((int) (32 + Integer.BYTES + Long.BYTES + scriptSigSize + Integer.BYTES));
        bb.put(transactionHash);
        bb.putInt(outputIndex);
        bb.putLong(scriptSigSize);
        bb.put(scriptSig);
        bb.putInt(sequence);

        return bb.array();
    }

    public static Input fromBytes(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        byte[] transactionHash = new byte[32];
        bb.get(transactionHash, 0, 32);
        int outputIndex = bb.getInt();
        long scriptSigSize = bb.getLong();
        byte[] scriptSig = new byte[(int) scriptSigSize];
        bb.get(scriptSig, 0, (int) scriptSigSize); // Offset might be wrong
        int sequence = bb.getInt();

        return new Input(transactionHash, outputIndex, scriptSigSize, scriptSig, sequence);
    }

    public UTXOIdentifier toUTXUtxoIdentifier() {
        return new UTXOIdentifier(transactionHash, outputIndex);
    }
}
