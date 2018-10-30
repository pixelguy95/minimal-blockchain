package utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DifficultyAdjustment {

    private DifficultyAdjustment(){
        // No invocation possible
    }

    public static BigInteger calculateTarget(byte[] difficulty){

        int exponent = difficulty[0];
        difficulty[0] = 0;
        System.out.println(exponent);
        ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(difficulty, 0, 4));
        BigInteger coefficient = BigInteger.valueOf(wrapped.getInt());
        System.out.println(coefficient);
        BigInteger base = BigInteger.valueOf(2);

        return coefficient.multiply(base.pow((8 * (exponent - 3))));
    }

    public static byte[] toCompactFormat(BigInteger target){

        byte[] bits = target.toByteArray();

        printAsHex(bits);
        if(bits[0] < 0x7f){

        } else {
            bits = prependByte(bits, (byte)0 );
        }

        bits = prependByte(bits, (byte)bits.length);

        if(bits.length == 3){
            bits = postPendByte(bits, (byte)0);
        }

        printAsHex(bits);

        return bits;
    }

    /**
     * Not very optimizted, does not matter if compact form doesnt work anyway.
     */
    private static byte[] prependByte(byte[] origin, byte b){

        byte[] output = new byte[1 + origin.length];

        output[0] = b;

        for(int i = 1; i < (origin.length+1);i++){
            output[i] = origin[i-1];
        }
        return output;
    }

    private static byte[] postPendByte(byte[] origin, byte b){

        byte[] output = new byte[1 + origin.length];

        for(int i = 0; i < (origin.length);i++){
            output[i] = origin[i];
        }

        output[3] = b;
        return output;
    }

    private static void printAsHex(byte[] hex)
    {
        for(int i=0; i<hex.length; i++)
        {
            System.out.print(String.format("0x%02X", (hex[i] & 0xFF)) + " ");
        }
        System.out.println();
    }

    private static byte[] toByteArray(long value) {
        return  ByteBuffer.allocate(8).putLong(value).array();
    }

    private static long fromByteArray(byte[] value) {
        ByteBuffer wrapped = ByteBuffer.wrap(value);
        return wrapped.getLong();
    }


    public static long calculateNewTarget(byte[] oldTarget, int minutesLastPeriod){

        return fromByteArray(oldTarget) * (minutesLastPeriod / 20160);
    }
    
}
