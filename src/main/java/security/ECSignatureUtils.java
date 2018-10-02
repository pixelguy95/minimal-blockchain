package security;

import java.security.*;

public class ECSignatureUtils {

    public static byte[] sign(byte[] plain, PrivateKey privateKey) {

        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(plain);
            return signature.sign();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static boolean verify(byte[] sign, byte[] plain, PublicKey pub) {

        Signature signature = null;
        try {
            signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(pub);

            signature.update(plain);

            return signature.verify(sign);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        return false;
    }
}
