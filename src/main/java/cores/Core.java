package cores;

import org.bouncycastle.util.encoders.Base64;
import security.ECKeyManager;

import java.security.*;

public class Core {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        System.out.println("Hello world!");

        KeyPair pair = ECKeyManager.generateNewKeyPair();
        System.out.println(new String(Base64.encode(pair.getPrivate().getEncoded())));


        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(pair.getPrivate());

        signature.update("SIGN ME".getBytes());

        byte[] signed = signature.sign();

        signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(pair.getPublic());

        signature.update("SIGN ME".getBytes());

        System.out.println(signature.verify(signed));

    }


}
