package security;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

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

    public static void writeKeyToFile(PrivateKey privateKey, String path) throws IOException {

        try {
            DataOutputStream os = new DataOutputStream(new FileOutputStream(path));
            os.write(privateKey.getEncoded());
            os.close();
        } catch (Exception e) {
            System.err.println("Something went wrong during file writing:\n" + e.getMessage());
        }
    }

    public static PrivateKey loadKeyFromFile(String path) {

        return null;
    }
}
