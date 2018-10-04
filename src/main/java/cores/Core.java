package cores;

import domain.transaction.Input;
import domain.transaction.Output;
import domain.transaction.Transaction;
import io.nayuki.bitcoin.crypto.Base58Check;
import io.nayuki.bitcoin.crypto.Ripemd160;
import io.nayuki.bitcoin.crypto.Sha256Hash;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.encoders.Base64;
import script.ScriptBuilder;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;
import java.util.List;


/**
 * This is just garbage for testing certain functions
 */
public class Core {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        byte[] fakePartial = DigestUtils.sha256("This will be the partial hash".getBytes());
        byte[] fakeTransactionHash = DigestUtils.sha256("This will be the full hash".getBytes());

        KeyPair kp = ECKeyManager.generateNewKeyPair();
        byte[] signature = ECSignatureUtils.sign(fakePartial, kp.getPrivate());
        byte[] publicKey = kp.getPublic().getEncoded();

        byte[] sha = DigestUtils.sha256(publicKey);
        byte[] rip = Ripemd160.getHash(sha);

        System.out.println(Base58Check.bytesToBase58(rip));

        byte[] scriptSig = ScriptBuilder.newScript().writeToStack(signature).writeToStack(publicKey).end();
        byte[] scriptPubKey = ScriptBuilder.newScript().dup().hash160().writeToStack(rip).equalVerify().checkSig().end();

        Input input = new Input(fakeTransactionHash, 0, scriptSig.length, scriptSig, 0xFFFFFFFF);
        List<Input> inputs = Arrays.asList(input);

        Output output = new Output(10, scriptPubKey.length, scriptPubKey);
        List<Output> outputs = Arrays.asList(output);

        Transaction transaction1 = new Transaction(1, (short) 0, 1, 1, inputs, outputs, null, 0xFFFFFFFF);
    }


}
