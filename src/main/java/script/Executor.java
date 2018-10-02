package script;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Stack;

public class Executor {

    private byte[] program;
    private Stack<byte[]> executionStack;

    public Executor(byte[] program) {
        this.program = program;
        executionStack = new Stack<>();
    }

    public void executeAll() {
        ByteBuffer bb = ByteBuffer.wrap(program);

        while (bb.hasRemaining()) {
            int op = unsignedToBytes(bb.get());

            if (op <= OpCodes.OP_16 && op >= OpCodes.OP_1) {
                executionStack.push(ByteBuffer.allocate(4).putInt(op - 0x50).array());
                System.out.println("Push " + (op - 0x50));
                continue;
            }

            int pushMe = 0;
            int a;
            int b;
            byte[] cA;
            byte[] cB;
            switch (op) {
                case OpCodes.OP_0:
                    executionStack.push(ByteBuffer.allocate(4).putInt(pushMe).array());
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
            }
        }
    }

    public void printTopValueAtStack() {
        System.out.println(new BigInteger(executionStack.peek()).toString(16));
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }
}
