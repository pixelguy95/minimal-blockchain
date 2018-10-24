package domain.transaction;

import java.util.Arrays;
import java.util.List;

/**
 * Every block has one coinbase transaction, this transaction has no inputs and only one output.
 * The output amount should be equal to the fee + the block reward.
 */
public class CoinbaseTransaction extends Transaction {

    public CoinbaseTransaction(int version, short flag, Output output, List<Witness> witnesses, int lockTime) {
        super(version, flag, 0, 1, Arrays.asList(), Arrays.asList(output), witnesses, lockTime);
    }
}
