package domain.transaction;

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

    public int locktime;

}
