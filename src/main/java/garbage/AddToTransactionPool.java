package garbage;

import apis.domain.Host;
import apis.domain.requests.HandshakeRequest;
import apis.domain.requests.NewTransactionRequest;
import apis.domain.responses.HandshakeResponse;
import apis.utils.TransactionRESTWrapper;
import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;
import utils.RESTUtils;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

public class AddToTransactionPool {

    public static void main(String args[]) throws IOException {

        byte[] fakePartial = DigestUtils.sha256("This will be the partial hash".getBytes());
        byte[] fakeTransactionHash = DigestUtils.sha256("This will be the full hash".getBytes());

        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(fakePartial, kp.getPrivate());
        byte[] publicKey = kp.getPublic().getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        Input input = new Input(fakeTransactionHash, 0, scriptSig.length, scriptSig, 0xFFFFFFFF);
        List<Input> inputs = Arrays.asList(input);

        Output output = new Output(10, scriptPubKey.length, scriptPubKey);
        List<Output> outputs = Arrays.asList(output);

        Transaction t = new Transaction(1, (short) 0, 1, 1, inputs, outputs, null, 0xFFFFFFFF);

        TransactionRESTWrapper.sendTransaction(new Host("localhost:30109"), t);
    }
}
