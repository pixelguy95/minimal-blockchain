package utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DifficultyAdjustment {

    private DifficultyAdjustment(){
        // No invocation possible
    }

    public static byte[] calculateTarget(byte[] difficulty){

        int exponent = difficulty[0];

        ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(difficulty, 1, 4));
        int coefficient = wrapped.getInt();

        return DifficultyAdjustment.toByteArray(coefficient * (long)Math.pow(2, (8 * (exponent - 1))));
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
