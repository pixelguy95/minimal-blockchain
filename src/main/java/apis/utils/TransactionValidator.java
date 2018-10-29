package apis.utils;

import apis.static_structures.Blockchain;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import domain.utxo.UTXOIdentifier;
import org.apache.commons.lang.ArrayUtils;
import script.ScriptExecutor;

public class TransactionValidator {
    private UTXO utxo;
    private Blockchain blockchain;
    private TransactionPool transactionPool;

    public TransactionValidator(UTXO utxo, Blockchain blockchain, TransactionPool transactionPool) {
        this.utxo = utxo;
        this.blockchain = blockchain;
        this.transactionPool = transactionPool;
    }

    public Result validateTransaction(Transaction transaction) {

        long inputSum = 0;
        for(Input i : transaction.inputs) {
            if(!utxo.has(new UTXOIdentifier(i.transactionHash, i.outputIndex))) {
                return new Result("One of the inputs ws not found as UTXO");
            } else {
                if(utxo.busy.contains(new UTXOIdentifier(i.transactionHash, i.outputIndex))) {
                    return new Result("One of the inputs is already used in a non-safe block");
                }
            }

            Output o = utxo.get(new UTXOIdentifier(i.transactionHash, i.outputIndex));
            inputSum += o.amount;
        }

        long outputSum = transaction.outputs.stream().mapToLong(o->o.amount).sum();
        if(inputSum < outputSum) {
            return new Result("The sum of inputs was not enough to cover sum of outputs");
        }

        for(Input i : transaction.inputs) {
            Output o = utxo.get(new UTXOIdentifier(i.transactionHash, i.outputIndex));
            Transaction t = blockchain.getTransactionByTXID(i.transactionHash);

            byte[] combinedScript = ArrayUtils.addAll(i.scriptSig, o.scriptPubKey);
            if(!ScriptExecutor.executeWithCheckSig(combinedScript, t.partialHash(i.outputIndex))) {
                return new Result("One of the scriptSigs did not validate toward it's respective scriptPubKey");
            }
        }

        return new Result();
    }

    public static class Result {
        public String resaon = "";
        public boolean passed = true;

        public Result(String resaon) {
            passed = false;
            this.resaon = resaon;
        }

        public Result() {
        }
    }
}
