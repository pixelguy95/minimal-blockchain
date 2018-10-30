package apis.utils;


import apis.static_structures.Blockchain;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import domain.Validatable;
import domain.block.Block;

public class BlockValidator implements Validator{
    private UTXO utxo;
    private Blockchain blockchain;
    private TransactionPool transactionPool;

    public BlockValidator(UTXO utxo, Blockchain blockchain, TransactionPool transactionPool) {
        this.utxo = utxo;
        this.blockchain = blockchain;
        this.transactionPool = transactionPool;
    }

    @Override
    public Result validate(Validatable v) {
        Block block = (Block)v;

        return new Result();
    }
}
