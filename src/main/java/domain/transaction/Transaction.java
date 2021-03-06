package domain.transaction;

import apis.domain.Host;
import apis.static_structures.Blockchain;
import apis.static_structures.UTXO;
import apis.utils.wrappers.TransactionRESTWrapper;
import domain.Validatable;
import domain.utxo.UTXOIdentifier;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import script.ScriptBuilder;
import security.ECSignatureUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * https://en.bitcoin.it/wiki/Transaction
 */
public class Transaction implements Serializable, Validatable {
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

        if(bytes == null || bytes.length == 0) {
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes);

        int version = bb.getInt();
        short flag = bb.getShort();
        long inCounter = bb.getLong();

        List<Input> inputs = new ArrayList<>();
        for(int i = 0; i < inCounter; i++) {
            int size = bb.getInt();
            byte[] data = new byte[size];
            bb.get(data, 0, size);
            inputs.add(Input.fromBytes(data));
        }

        long outCounter = bb.getLong();
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

    public byte[] fullHash(){
        return DigestUtils.sha256(serialize());
    }

    public byte[] partialHash(int outIndex) {
        return DigestUtils.sha256(new Transaction(version, flag, inCounter, outCounter, inputs, Arrays.asList(outputs.get(outIndex)), witnesses, lockTime).serialize());
    }

    public boolean equals(Object o) {
        return ByteBuffer.wrap(serialize()).equals(ByteBuffer.wrap(((Transaction)o).serialize()));
    }

    public long getFee(UTXO utxo) {

        long in = 0;
        for(Input i : inputs) {
            in += utxo.get(new UTXOIdentifier(i.transactionHash, i.outputIndex)).amount;
        }

        long out = 0;
        for(Output o : outputs) {
            out += o.amount;
        }

        return in - out;
    }

    /**
     * TODO: Remove this when better methods exists
     * @return
     */
    public static Transaction makeFakeTransaction(PrivateKey priv, PublicKey pub) {
        byte[] fakePartial = DigestUtils.sha256("This will be the partial hash".getBytes());
        byte[] fakeTransactionHash = DigestUtils.sha256("This will be the full hash".getBytes());

        byte[] signature = ECSignatureUtils.sign(fakePartial, priv);
        byte[] publicKey = pub.getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        Input input = new Input(fakeTransactionHash, 0, scriptSig.length, scriptSig, 0xFFFFFFFF);
        List<Input> inputs = Arrays.asList(input);

        Output output = new Output(10, scriptPubKey.length, scriptPubKey);
        List<Output> outputs = Arrays.asList(output);

        return new Transaction(1, (short) 0, 1, 1, inputs, outputs, null, 0xFFFFFFFF);
    }

    public static Transaction makeTransactionFromOutputs(Blockchain blockchain, KeyPair yourKeys, List<UTXOIdentifier> outputIDs, PublicKey reciverPub, long amount) {
        List<Transaction> refTransactions = new ArrayList<>();
        for(UTXOIdentifier id : outputIDs) {
            refTransactions.add(blockchain.getTransactionByTXID(id.txid));
        }

        List<Output> all = new ArrayList<>();
        for(int i = 0; i < refTransactions.size(); i++) {
            all.add(refTransactions.get(i).outputs.get(outputIDs.get(i).outputIndex));
        }

        List<OutputWithID> included = new ArrayList<>();
        long temp = amount;
        for(int i = 0; i < all.size(); i++) {
            temp-=all.get(i).amount;
            included.add(new OutputWithID(outputIDs.get(i), all.get(i)));
            if(temp < 0)
                break;
        }

        //Not possible to make this transaction from these outputs
        if(temp > 0) {
            return null;
        }

        List<Input> inputs = createInputs(yourKeys, (List<Transaction>) refTransactions, (List<OutputWithID>) included);

        //Recivers output
        byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(reciverPub);
        Output newOutput = new Output(amount, scriptPubKey.length, scriptPubKey);

        //Calculate change
        long sumOfInputs = included.stream().mapToLong(i->i.output.amount).sum();

        //TODO: Fee?

        //Change output
        byte[] scriptPubKeyChange = ScriptBuilder.generateP2PKScript(yourKeys.getPublic());
        Output changeOutput = new Output(sumOfInputs - amount, scriptPubKeyChange.length, scriptPubKeyChange);


        return new Transaction(1, (short) 0, inputs.size(), 2, inputs, Arrays.asList(newOutput, changeOutput), null, 0xFFFFFFFF);
    }

    public static Transaction makeTransactionFromOutputs(Host host, KeyPair yourKeys, List<UTXOIdentifier> outputIDs, String reciverPub, long amount) {
        List<Transaction> refTransactions = new ArrayList<>();
        for(UTXOIdentifier id : outputIDs) {
            refTransactions.add(TransactionRESTWrapper.getTransactionFromChain(host, id.txid).transaction);
        }

        List<Output> all = new ArrayList<>();
        for(int i = 0; i < refTransactions.size(); i++) {
            all.add(refTransactions.get(i).outputs.get(outputIDs.get(i).outputIndex));
        }

        List<OutputWithID> included = new ArrayList<>();
        long temp = amount;
        for(int i = 0; i < all.size(); i++) {
            temp-=all.get(i).amount;
            included.add(new OutputWithID(outputIDs.get(i), all.get(i)));
            if(temp < 0)
                break;
        }

        //Not possible to make this transaction from these outputs
        if(temp > 0) {
            return null;
        }

        List<Input> inputs = createInputs(yourKeys, refTransactions, included);

        //Recivers output
        byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(Base64.getUrlDecoder().decode(reciverPub));
        Output newOutput = new Output(amount, scriptPubKey.length, scriptPubKey);

        //Calculate change
        long sumOfInputs = included.stream().mapToLong(i->i.output.amount).sum();

        //TODO: Fee?

        //Change output
        byte[] scriptPubKeyChange = ScriptBuilder.generateP2PKScript(yourKeys.getPublic());
        Output changeOutput = new Output(sumOfInputs - amount, scriptPubKeyChange.length, scriptPubKeyChange);

        return new Transaction(1, (short) 0, inputs.size(), 2, inputs, Arrays.asList(newOutput, changeOutput), null, 0xFFFFFFFF);
    }

    private static List<Input> createInputs(KeyPair yourKeys, List<Transaction> refTransactions, List<OutputWithID> included) {

        List<Input> resultInputs = new ArrayList<>();
        for(int i = 0; i < included.size(); i++) {
            byte[] partial = refTransactions.get(i).partialHash(included.get(i).id.outputIndex);

            byte[] signature = ECSignatureUtils.sign(partial, yourKeys.getPrivate());
            byte[] scriptSig = ScriptBuilder.generateP2PKSignature(signature, yourKeys.getPublic());

            if(refTransactions.get(i) instanceof CoinbaseTransaction) {
                CoinbaseTransaction cbt = (CoinbaseTransaction) refTransactions.get(i);
                resultInputs.add(new Input(cbt.fullHash(),
                        included.get(i).id.outputIndex,
                        scriptSig.length,
                        scriptSig,
                        0xFFFFFFFF));
            } else {
                resultInputs.add(new Input(refTransactions.get(i).fullHash(),
                        included.get(i).id.outputIndex,
                        scriptSig.length,
                        scriptSig,
                        0xFFFFFFFF));
            }
        }

        return resultInputs;
    }

    private static class OutputWithID {
        public UTXOIdentifier id;
        public Output output;

        public OutputWithID(UTXOIdentifier id, Output output) {
            this.id = id;
            this.output = output;
        }
    }
}
