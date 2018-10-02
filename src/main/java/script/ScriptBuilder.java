package script;

import sun.font.Script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScriptBuilder {
    private ByteArrayOutputStream bb;

    private  ScriptBuilder(ByteArrayOutputStream bb) {
        this.bb = bb;
    }

    public static ScriptBuilder newScript() {
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        return new ScriptBuilder(bb);
    }

    public ScriptBuilder writeToStack(byte[] bytes) {
        try {
            bb.write(OpCodes.OP_PUSHDATA4);
            bb.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
            bb.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public ScriptBuilder writeIntToStack(int n) {
        if(n == 0) {
            bb.write(OpCodes.OP_0);
        }

        if(n > 0 && n < 17) {
            bb.write(n+0x50);
        } else {
            bb.write(OpCodes.OP_PUSHDATA1);
            bb.write(Integer.BYTES);
            try {
                bb.write(ByteBuffer.allocate(4).putInt(n).array());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public ScriptBuilder add() {
        bb.write(OpCodes.OP_ADD);
        return this;
    }

    public ScriptBuilder equals() {
        bb.write(OpCodes.OP_EQUAL);
        return this;
    }

    public ScriptBuilder equalVerify() {
        bb.write(OpCodes.OP_EQUALVERIFY);
        return this;
    }

    public byte[] end() {
        return bb.toByteArray();
    }
}
