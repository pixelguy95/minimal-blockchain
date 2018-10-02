package security;

import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ECKeyManager {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static KeyPair generateNewKeyPair() {
        try {
            ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256k1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
            g.initialize(ecGenSpec, new SecureRandom());
            return g.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Something went wrong during key generation:\n" + e.getMessage());
            return null;
        }
    }

    public static void writePairToFile(KeyPair pair, String path) {

        try {
            DataOutputStream os = new DataOutputStream(new FileOutputStream(path));
            os.write(pair.getPrivate().getEncoded());
            os.write(pair.getPublic().getEncoded());
            os.close();
        } catch (Exception e) {
            System.err.println("Something went wrong during file writing:\n" + e.getMessage());
        }
    }

    public static KeyPair loadPairFromFile(String path) {

        try {
            DataInputStream os = new DataInputStream(new FileInputStream(path));
            byte[] privateKey = new byte[144];
            byte[] publicKey = new byte[88];
            os.read(privateKey, 0, 144);
            os.read(publicKey, 0, 88);
            os.close();

            KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
            PublicKey pubK = fact.generatePublic(new X509EncodedKeySpec(publicKey));
            PrivateKey privKey = fact.generatePrivate(new PKCS8EncodedKeySpec(privateKey));

            return new KeyPair(pubK, privKey);

        } catch (Exception e) {
            System.err.println("Something went wrong during file reading:\n" + e.getCause());
        }

        return null;
    }

    public static PublicKey bytesToPublicKey(byte[] key) {
        try {
            KeyFactory fact = KeyFactory.getInstance("ECDSA", "BC");
            return fact.generatePublic(new X509EncodedKeySpec(key));

        } catch (Exception e) {
            System.err.println("Something went wrong during file reading:\n" + e.getCause());
        }

        return null;
    }
}
