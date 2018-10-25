package domain.utxo;

import com.google.common.base.Objects;

import java.nio.ByteBuffer;

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
        return Objects.hashCode(txid, outputIndex);
    }

    public byte[] serialize() {
        ByteBuffer fill = ByteBuffer.allocate(32+Integer.BYTES);
        fill.put(txid);
        fill.putInt(outputIndex);

        return fill.array();
    }
}
