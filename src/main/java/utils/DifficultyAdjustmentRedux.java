package utils;

import apis.static_structures.Blockchain;
import domain.block.Block;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

public class DifficultyAdjustmentRedux {

    public static final int TARGET_BLOCK_TIME = 300;
    public static final int RECALCULATE_HEIGHT = 20;

    public static long getNextBlockBits(Blockchain bchain){

        if(bchain.getChain().size() == 0) {
            return 0x1f000bff; // The difficulty of the genesis block
        }

        Block top = bchain.getTopBlock();

        if((bchain.getChain().get(ByteBuffer.wrap(top.header.getHash())).height + 1)  % RECALCULATE_HEIGHT != 0 ){
            return top.header.bits;
        }

        long bcheight = bchain.getChain().get(ByteBuffer.wrap(top.header.getHash())).height + 1;
        long bits = bchain.getChain().get(ByteBuffer.wrap(top.header.getHash())).blockHeader.bits;

        Block b = bchain.getBlock(top.header.prevBlockHash);

        while(b != null) {
            if(bchain.getChain().get(ByteBuffer.wrap(b.header.getHash())).height == bcheight - RECALCULATE_HEIGHT){
                return calculateTarget(top.header.time, bchain.getChain().get(ByteBuffer.wrap(b.header.getHash())).blockHeader.time, bits);
            }

            b = bchain.getBlock(b.header.prevBlockHash);
        }
        return -1;
    }

    /**
     * Same as above but starts counting from the given block and given height
     *
     * Used when validating blocks that have not been added yet
     *
     * @param bchain
     * @param top
     * @return
     */
    public static long getBlockBits(Blockchain bchain, Block top) {

        if(bchain.getChain().size() == 0) {
            return 0x1f000bff; // The difficulty of the genesis block
        }

        long height = bchain.getChain().get(ByteBuffer.wrap(top.header.getHash())).height;

        if((height + 1) % RECALCULATE_HEIGHT != 0 ){
            return top.header.bits;
        }

        long bcheight = height + 1;
        long bits = top.header.bits;

        Block b = bchain.getBlock(top.header.prevBlockHash);

        while(b != null) {
            if(bchain.getChain().get(ByteBuffer.wrap(b.header.getHash())).height == bcheight - RECALCULATE_HEIGHT){
                return calculateTarget(top.header.time, bchain.getChain().get(ByteBuffer.wrap(b.header.getHash())).blockHeader.time, bits);
            }

            b = bchain.getBlock(b.header.prevBlockHash);
        }
        return -1;
    }

    public static long calculateTarget(long lastBlockTime, long compareBlockTime, long bits) {

        BigInteger target = toTarget(bits);
        long blockTimeDiff = lastBlockTime - compareBlockTime;
        double div =  (double)(TARGET_BLOCK_TIME * RECALCULATE_HEIGHT) / (double)(blockTimeDiff);
        //System.out.println("OPTIMAL / CURRENT = " + div);
        BigInteger newTarget = new BigDecimal(target).divide(new BigDecimal(div), 10, RoundingMode.HALF_EVEN).toBigInteger();

        return toCompactBits(newTarget);
    }

    public static BigInteger toTarget(long bits) {
        //nWord * 2^(8*(nSize - 3))
        long nSize = (bits >> 24) & 0xFF;
        long nWord = bits & 0x007fffff;
        return new BigInteger(String.valueOf(nWord)).multiply(new BigInteger("2").pow((int) ((nSize-3) * 8)));
    }

    public static long toCompactBits(BigInteger target) {

        int nSize = (target.bitLength() + 7) / 8;
        long nCompact = 0;

        if(nSize <= 3) {
            nCompact = target.and(new BigInteger("000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFF", 16)).shiftLeft(8*(3-nSize)).longValue();
        } else {
            BigInteger copy = new BigInteger(target.toByteArray()).shiftRight(8*(nSize-3));
            nCompact = copy.and(new BigInteger("000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFF", 16)).longValue();
        }

        if((nCompact & 0x00800000) > 0) {
            nCompact = nCompact >> 8;
            nSize++;
        }

        nCompact = nCompact | (nSize << 24);
        return nCompact;
    }
}
