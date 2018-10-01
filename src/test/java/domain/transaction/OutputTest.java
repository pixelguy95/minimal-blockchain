package domain.transaction;

import org.junit.Before;
import org.junit.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class OutputTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSerialization() throws Exception {
        Output o1 = new Output(100, 3, new byte[]{20, 40, 127});
        byte[] serial = o1.serialize();

        Output o2 = Output.fromBytes(serial);

        assertEquals(o2.amount, o1.amount);
        assertEquals(o2.scriptPubKeyLength, o1.scriptPubKeyLength);
        assertArrayEquals(o2.scriptPubKey, o1.scriptPubKey);
    }
}