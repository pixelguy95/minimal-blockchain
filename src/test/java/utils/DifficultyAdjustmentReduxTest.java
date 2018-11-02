package utils;

import apis.static_structures.Blockchain;
import db.DBHolder;
import domain.block.Block;
import node.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import security.ECKeyManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.KeyPair;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class DifficultyAdjustmentReduxTest {

    @Test
    public void getNextBlockBits() {
    }

    @Test
    public void toLarge() {
        assertEquals(DifficultyAdjustmentRedux.toTarget(0x1b0404cb), new BigInteger("00000000000404CB000000000000000000000000000000000000000000000000", 16));
    }

    @Test
    public void toCompactBits() {

        BigInteger large = DifficultyAdjustmentRedux.toTarget(0x1b0404cb);
        assertEquals(large, new BigInteger("404CB000000000000000000000000000000000000000000000000", 16));

        long compact = DifficultyAdjustmentRedux.toCompactBits(large);
        assertEquals(compact, 0x1b0404cb);
    }

    /*
     * Example is from actual time period between block
     * 100800 - 102816
     * Jan 3, 2011 6:10:11 AM - Jan 15, 2011 3:26:07 PM
     * 1294035011 - 1295105167
     *
     * Jan 3, 2011 6:13:47 AM
     *
     */
    @Test
    public void calculateTarget() {
        long newBits = DifficultyAdjustmentRedux.calculateTarget(1295105167, 1294035227, 0x1b0404cb);
        System.out.println(new BigInteger("1b0404cb", 16).toString(16));
        System.out.println(new BigInteger(String.valueOf(newBits)).toString(16));

        newBits = DifficultyAdjustmentRedux.calculateTarget(2016*600, 0, 0x1b0404cb);
        assertEquals(0x1b0404cb, newBits);

        newBits = DifficultyAdjustmentRedux.calculateTarget(2016*601, 0, 0x1b0404cb);
        assertTrue(0x1b0404cb < newBits);

        newBits = DifficultyAdjustmentRedux.calculateTarget(2016*599, 0, 0x1b0404cb);
        assertTrue(0x1b0404cb > newBits);
    }

    @Test
    public void testAdjustment() {
        final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-wth1"};

        Config config = new Config(initialNodeArgs);

        DBHolder dbs = new DBHolder(config.dbFolder);

        dbs.destroy(config.dbFolder);
        dbs.restart(config.dbFolder);

        KeyPair pair = ECKeyManager.generateNewKeyPair();

        Block genesis = new Block(new ArrayList<>(), DigestUtils.sha256("NO PREVIOUS BLOCK".getBytes()), pair.getPublic());
        genesis.header.time = 1294035011;
        Blockchain blockchain = new Blockchain(dbs.getBlockDB(), dbs.getBlockHeaderDB(), dbs.getMetaDB(), config, genesis);


        long diff = 1295105167 - 1294035011;
        long current = 1294035011 + diff;
        assert pair != null;
        for(int i = 0; i < 2015; i++){
            Block b = new Block(new ArrayList<>(), blockchain.getTopBlock().header.getHash(), pair.getPublic());
            b.header.bits = 0x1b0404cb;
            b.header.time = current;
            current+=diff;
            blockchain.addBlock(b);
        }

        long newBits = DifficultyAdjustmentRedux.getNextBlockBits(blockchain, 1295105167);
        assertTrue(0x1b0404cb > newBits);

        System.out.println("OPTIMAL BLOCKTIME / TEST BLOCK TIME");
        System.out.println((double)(DifficultyAdjustmentRedux.TARGET_BLOCK_TIME * DifficultyAdjustmentRedux.RECALCULATE_HEIGHT) / (double)(1295105167 - 1294035011) );
        System.out.println();

        System.out.println("OLD COMPACT, NEW COMPACT");
        System.out.println(new BigInteger(String.valueOf(0x1b0404cb)).toString(16));
        System.out.println(new BigInteger(String.valueOf(newBits)).toString(16));
        System.out.println();

        System.out.println("OLD TARGET, NEW TARGET");
        System.out.println(DifficultyAdjustmentRedux.toTarget(0x1b0404cb).toString(16));
        System.out.println(DifficultyAdjustmentRedux.toTarget(newBits).toString(16));
        System.out.println();

        System.out.println("OLD TARGET / NEW TARGET");
        System.out.println(new BigDecimal(DifficultyAdjustmentRedux.toTarget(0x1b0404cb))
                .divide(new BigDecimal(DifficultyAdjustmentRedux.toTarget(newBits)), 16, RoundingMode.HALF_EVEN).toString());
    }
}