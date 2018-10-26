package domain.utxo;

import com.google.common.base.Objects;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.transaction.Witness;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UTXOIdentifier {
    public byte[] txid;
    public int outputIndex;

    public UTXOIdentifier(byte[] txid, int outputIndex) {
        this.txid = txid;
        this.outputIndex = outputIndex;
    }

    public boolean equals(Object o) {
        UTXOIdentifier other = (UTXOIdentifier)o;
        return ByteBuffer.wrap(this.txid).equals(ByteBuffer.wrap(other.txid)) && this.outputIndex == other.outputIndex;
    }

    public int hashCode() {
        return Objects.hashCode(ByteBuffer.wrap(txid), outputIndex);
    }

    public byte[] serialize() {
        ByteBuffer fill = ByteBuffer.allocate(32+Integer.BYTES);
        fill.put(txid);
        fill.putInt(outputIndex);

        return fill.array();
    }

    public static UTXOIdentifier fromBytes(byte[] bytes) {
        if(bytes == null || bytes.length == 0) {
            return null;
        }

        byte[] txid = new byte[32];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.get(txid, 0, 32);
        int outptIndex = bb.getInt();

        return new UTXOIdentifier(txid, outptIndex);
    }
}
