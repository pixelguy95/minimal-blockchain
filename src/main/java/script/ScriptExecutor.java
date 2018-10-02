package script;

import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import security.ECKeyManager;
import security.ECSignatureUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Stack;

public class ScriptExecutor {
    public static boolean executeWithCheckSig(byte[] program, byte[] m) {
        Stack<byte[]> executionStack = new Stack<>();

        ByteBuffer bb = ByteBuffer.wrap(program);

        while (bb.hasRemaining()) {
            int op = unsignedToBytes(bb.get());

            if (op <= OpCodes.OP_16 && op >= OpCodes.OP_1) {
                executionStack.push(ByteBuffer.allocate(4).putInt(op - 0x50).array());
                System.out.println("Push " + (op - 0x50));
                continue;
            }

            printStack(executionStack);
            int pushMe = 0;
            int a;
            int b;
            byte[] cA;
            byte[] cB;
            byte[] data;
            switch (op) {
                case OpCodes.OP_0:
                    executionStack.push(ByteBuffer.allocate(4).putInt(pushMe).array());
                    break;
                case OpCodes.OP_PUSHDATA4:
                    int size = bb.getInt();
                    data = new byte[size];
                    bb.get(data, 0, size);
                    executionStack.push(data);
                    System.out.println("Push " + new BigInteger(data).toString(16));
                    break;

                case OpCodes.OP_ADD:
                    a = ByteBuffer.wrap(executionStack.pop()).getInt();
                    b = ByteBuffer.wrap(executionStack.pop()).getInt();
                    executionStack.push(ByteBuffer.allocate(4).putInt(a + b).array());
                    System.out.println("Add " + a + " " + b);
                    break;
                case OpCodes.OP_EQUAL:
                    cA = executionStack.pop();
                    cB = executionStack.pop();
                    System.out.println("Equals " + new BigInteger(cA).toString(16) + " " + new BigInteger(cB).toString(16));
                    executionStack.push(ByteBuffer.allocate(1).put((byte) (Arrays.equals(cA, cB) ? 1 : 0)).array());
                    break;
                case OpCodes.OP_EQUALVERIFY:
                    cA = executionStack.pop();
                    cB = executionStack.pop();

                    System.out.println("Equal verify");
                    System.out.println("a " + new BigInteger(cA).toString(16));
                    System.out.println("b " + new BigInteger(cB).toString(16));
                    if(!(Arrays.equals(cA, cB))) {
                        return false;
                    }
                    break;
                case OpCodes.OP_DUP:
                    cA = executionStack.peek();
                    executionStack.push(cA);
                    break;
                case OpCodes.OP_HASH160:
                    data = executionStack.pop();
                    byte[] sha = DigestUtils.sha256(data);
                    byte[] rip = Ripemd160.getHash(sha);
                    executionStack.push(rip);
                    break;

                case OpCodes.OP_CHECKSIG:
                    cA = executionStack.pop();
                    cB = executionStack.pop();

                    System.out.println("Check sig");
                    System.out.println("a " + new BigInteger(cA).toString(16));
                    System.out.println("b " + new BigInteger(cB).toString(16));
                    PublicKey pk = ECKeyManager.bytesToPublicKey(cA);
                    boolean verified = ECSignatureUtils.verify(cB, m, pk);

                    if(!verified) {
                        return false;
                    }

                    executionStack.push(ByteBuffer.allocate(1).put((byte) (verified ? 1 : 0)).array());
                    break;
            }
        }

        if(executionStack.size() == 1 && ByteBuffer.wrap(executionStack.peek()).get() != 0) {
            return true;
        }

        return false;
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public static void printStack(Stack<byte[]> stack) {
        System.out.println("===================");
        for (byte[] b : stack) {
            System.out.println(new BigInteger(b).toString(16));
        }
        System.out.println("===================");
    }
}
