package utils;

import domain.block.BlockHeader;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class DifficultyAdjustmentTest {

    @Test
    /*
     * As per block 277.316 in the bitcoin blockchain
     * https://blockexplorer.com/block/0000000000000001b6b9a13b095e96db41c4a928b97ef2d944a9b31b2cc7bdc4
     *
     * Equation given
     * https://github.com/bitcoinbook/bitcoinbook/blob/develop/ch10.asciidoc
     *
     */
    public void testDifficultyToTargetConversion() {

        assertEquals(new BigInteger("22829202948393929850749706076701368331072452018388575715328"), DifficultyAdjustment.calculateTarget(hexStringToByteArray("1903a30c")));
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    @Test
    public void removeLater(){
        mineBlock();
    }

    protected void mineBlock()  {

        // Blockheader should not be created here but...

        // "1d00ffff" is the difficulty bits Satoshi chose back in the day
        BlockHeader bh = new BlockHeader(1, new byte[4], new byte[4], hexStringToByteArray("1d00ffff"));

        BigInteger target = DifficultyAdjustment.calculateTarget(bh.difficultyBits);

        int i = 0;
        while( (new BigInteger(bh.getHash()).compareTo(target)) < 0){
            bh.incrementNonce();
            if( i % 1000000 == 0 ){
                System.out.print(". ");
                if(i % 10000000 == 0){
                    System.out.println("");
                }
            }
            i++;
        }
        System.out.println("BLOCK SUCCESSFULLY MINED WITH HASH: " + new String(bh.getHash()));
    }

}