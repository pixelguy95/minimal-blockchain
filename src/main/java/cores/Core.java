package cores;

import io.nayuki.bitcoin.crypto.Sha256Hash;
import org.bouncycastle.util.encoders.Base64;
import security.ECKeyManager;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;

public class Core {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {

        System.out.println("Hello world!");

        KeyPair pair = ECKeyManager.generateNewKeyPair();

        ECKeyManager.writePairToFile(pair, "key.pair");
        KeyPair pair2 = ECKeyManager.loadPairFromFile("key.pair");

        System.out.println(pair.getPrivate().getEncoded().length);
        System.out.println(pair2.getPrivate().getEncoded().length);

        System.out.println(new BigInteger(pair.getPrivate().getEncoded()).toString(16));
        System.out.println(new BigInteger(pair2.getPrivate().getEncoded()).toString(16));
        System.out.println(new BigInteger(pair.getPublic().getEncoded()).toString(16));
        System.out.println(new BigInteger(pair2.getPublic().getEncoded()).toString(16));


//        System.out.println(new String(Base64.encode(pair.getPrivate().getEncoded())));
//
//
//        Signature signature = Signature.getInstance("SHA256withECDSA");
//        signature.initSign(pair.getPrivate());
//
//        signature.update("SIGN ME".getBytes());
//
//        byte[] signed = signature.sign();
//
//        signature = Signature.getInstance("SHA256withECDSA");
//        signature.initVerify(pair.getPublic());
//
//        signature.update("SIGN ME".getBytes());
//
//        System.out.println(signature.verify(signed));
    }


}
