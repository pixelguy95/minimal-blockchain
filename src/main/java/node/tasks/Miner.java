package node.tasks;

import domain.block.BlockHeader;
import utils.DifficultyAdjustment;

import java.math.BigInteger;

import java.util.concurrent.atomic.AtomicBoolean;

public class Miner extends AbstractTask {
    public Miner(AtomicBoolean keepAlive) {
        super(keepAlive);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    protected void mineBlock()  {
//
//        // Blockheader should not be created here but...
//
//        // "1d00ffff" is the difficulty bits Satoshi chose back in the day
//        BlockHeader bh = new BlockHeader(1, new byte[4], new byte[4], hexStringToByteArray("1d00ffff"));
//
//        BigInteger target = DifficultyAdjustment.calculateTarget(bh.difficultyBits);
//
//        while( (new BigInteger(bh.getHash()).compareTo(target)) < 0){
//            bh.incrementNonce();
//        }
//        System.out.println("BLOCK SUCCESSFULLY MINED WITH HASH: " + new String(bh.getHash()));
//    }

//    public static void main(){
//        Miner m = new Miner(new AtomicBoolean(true));
//
//        m.mineBlock();
//
//    }

    // GARBAGE THROW AWAY OR MOVE TO GENESIS BLOCK CREATION
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
