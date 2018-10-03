package domain.transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * https://en.bitcoin.it/wiki/Transaction
 */
public class Transaction {
    public int version;
    public short flag;
    public long inCounter;
    public long outCounter;

    public List<Input> inputs;
    public List<Output> outputs;
    public List<Witness> witnesses;

    public int lockTime;

    public Transaction(int version, short flag, long inCounter, long outCounter, List<Input> inputs, List<Output> outputs, List<Witness> witnesses, int lockTime) {
        this.version = version;
        this.flag = flag;
        this.inCounter = inCounter;
        this.outCounter = outCounter;
        this.inputs = inputs;
        this.outputs = outputs;
        this.witnesses = witnesses;
        this.lockTime = lockTime;
    }

    /**
     * Serialize, for putting in blocks
     * @return bytes
     */
    public byte[] serialize() {
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        try {
            bb.write(ByteBuffer.allocate(Integer.BYTES).putInt(version).array());
            bb.write(ByteBuffer.allocate(Short.BYTES).putShort(flag).array());

            bb.write(ByteBuffer.allocate(Long.BYTES).putLong(inCounter).array());
            for(Input p : inputs) {
                byte[] data = p.serialize();
                bb.write(ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array());
                bb.write(data);
            }

            bb.write(ByteBuffer.allocate(Long.BYTES).putLong(outCounter).array());
            for(Output p : outputs) {
                byte[] data = p.serialize();
                bb.write(ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array());
                bb.write(data);
            }

            if(flag == 1) {
                bb.write(ByteBuffer.allocate(Long.BYTES).putLong(outCounter).array());
                for(Witness p : witnesses) {
                    byte[] data = p.serialize();
                    bb.write(ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array());
                    bb.write(data);
                }
            }

            bb.write(ByteBuffer.allocate(Integer.BYTES).putInt(lockTime).array());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bb.toByteArray();
    }

    public static Transaction fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        int version = bb.getInt();
        short flag = bb.getShort();
        long inCounter = bb.getLong();
        long outCounter = bb.getLong();

        List<Input> inputs = new ArrayList<>();
        for(int i = 0; i < inCounter; i++) {
            int size = bb.getInt();
            byte[] data = new byte[size];
            bb.get(data, 0, size);
            inputs.add(Input.fromBytes(data));
        }

        List<Output> outputs = new ArrayList<>();
        for(int i = 0; i < outCounter; i++) {
            int size = bb.getInt();
            byte[] data = new byte[size];
            bb.get(data, 0, size);
            outputs.add(Output.fromBytes(data));
        }

        List<Witness> witnesses = new ArrayList<>();
        if(flag == 1) {
            for(int i = 0; i < inCounter; i++) {
                int size = bb.getInt();
                byte[] data = new byte[size];
                bb.get(data, 0, size);
                witnesses.add(Witness.fromBytes(data));
            }
        }
        int locktime = bb.getInt();

        return new Transaction(version, flag, inCounter, outCounter, inputs, outputs, witnesses, locktime);
    }
}
