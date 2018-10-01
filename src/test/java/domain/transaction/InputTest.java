package domain.transaction;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class InputTest {


    @Test
    public void testSerialization() throws Exception {
        Input i1 = new Input(DigestUtils.sha256("Bajs rubrik"), 0, 5, new byte[]{1,2,3,4,5}, 0xFFFFFFFF);
        byte[] serial = i1.serialize();

        Input i2 = Input.fromBytes(serial);

        assertEquals(i1.outputIndex, i2.outputIndex);
        assertEquals(i1.scriptSigSize, i2.scriptSigSize);
        assertArrayEquals(i1.scriptSig, i2.scriptSig);
        assertEquals(i1.sequence, i2.sequence);
        assertArrayEquals(i1.transactionHash, i2.transactionHash);
    }

}