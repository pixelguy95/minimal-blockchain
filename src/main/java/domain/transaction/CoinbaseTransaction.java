package domain.transaction;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Every block has one coinbase transaction, this transaction has no inputs and only one output.
 * The output amount should be equal to the fee + the block reward.
 */
public class CoinbaseTransaction extends Transaction {

    private long randPart = 0;

    public CoinbaseTransaction(int version, short flag, Output output, List<Witness> witnesses, int lockTime) {
        super(version, flag, 0, 1, Arrays.asList(), Arrays.asList(output), witnesses, lockTime);
        randPart = new Random().nextLong();
    }

    public byte[] fullHash(){
        byte[] serial = serialize();
        return DigestUtils.sha256(ByteBuffer.allocate(serial.length + Long.BYTES).put(serial).putLong(randPart).array());
    }

    public byte[] partialHash(int outIndex) {
        byte[] serial = new Transaction(version, flag, inCounter, outCounter, inputs, Arrays.asList(outputs.get(outIndex)), witnesses, lockTime).serialize();
        return DigestUtils.sha256(ByteBuffer.allocate(serial.length + Long.BYTES).put(serial).putLong(randPart).array());
    }
}
