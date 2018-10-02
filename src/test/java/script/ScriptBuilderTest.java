package script;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

public class ScriptBuilderTest {

    ScriptBuilder sb;
    @Before
    public void setUp() throws Exception {
        sb = ScriptBuilder.newScript();
    }

    @Test
    public void buildAdditionScript() {
        sb.writeIntToStack(3);
        sb.writeIntToStack(4);
        sb.add();
        sb.writeIntToStack(7);
        sb.equals();

        System.out.println("Script");
        System.out.println(new BigInteger(sb.end()).toString(16));

        Executor e = new Executor(sb.end());
        e.executeAll();
        e.printTopValueAtStack();
    }
}