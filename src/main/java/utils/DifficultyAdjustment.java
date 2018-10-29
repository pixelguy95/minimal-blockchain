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
