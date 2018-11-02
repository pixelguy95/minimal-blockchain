package utils;

import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import apis.static_structures.TransactionPool;
import db.DBHolder;
import domain.block.Block;
import domain.block.BlockHeader;
import node.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import security.ECKeyManager;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.ArrayList;

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

    @Test
    public void testTargetToBits(){

        assertEquals(new BigInteger("22829202948393929850749706076701368331072452018388575715328"),
                DifficultyAdjustment.calculateTarget(
                        DifficultyAdjustment.toCompactFormat(new BigInteger("22829202948393929850749706076701368331072452018388575715328"))));
    }

    // why not:
    // new BigInteger(s, 16).toByteArray();
    // ?
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
    public void blockBitsNotModulus2016Test(){
        final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-wth1"};

        Config config = new Config(initialNodeArgs);

        DBHolder dbs = new DBHolder(config.dbFolder);

        dbs.destroy(config.dbFolder);
        dbs.restart(config.dbFolder);

        Blockchain blockchain = new Blockchain(dbs.getBlockDB(), dbs.getBlockHeaderDB(), dbs.getMetaDB(), config);

        KeyPair pair = ECKeyManager.generateNewKeyPair();

        assert pair != null;
        Block b = new Block(new ArrayList<>(), blockchain.getTopBlock().header.getHash(), pair.getPublic());
        b.header.difficultyBits = hexStringToByteArray("1903a30c");
        blockchain.addBlock(b);

        assertArrayEquals(b.header.difficultyBits, DifficultyAdjustment.getNextBlockBits(blockchain, 0));
    }

    @Test

    /*
     * Example is from actual time period between block
     * 100800 - 102816
     * Jan 3, 2011 6:10:11 AM - Jan 15, 2011 3:26:07 PM
     * 1294035011 - 1295105167
     *
     */
    public void blockBitsModulus2016Test(){
        final String[] initialNodeArgs = new String[]{"-i", "-p", "13337", "-db", ".local-persistence-wth1"};

        Config config = new Config(initialNodeArgs);

        DBHolder dbs = new DBHolder(config.dbFolder);

        dbs.destroy(config.dbFolder);
        dbs.restart(config.dbFolder);

        KeyPair pair = ECKeyManager.generateNewKeyPair();

        Block genesis = new Block(new ArrayList<>(), DigestUtils.sha256("NO PREVIOUS BLOCK".getBytes()), pair.getPublic());
        genesis.header.time = 1294035011;
        Blockchain blockchain = new Blockchain(dbs.getBlockDB(), dbs.getBlockHeaderDB(), dbs.getMetaDB(), config, genesis);


        long diff = 1294035011 - 1295105167;
        long current = 1294035011 + diff;
        assert pair != null;
        for(int i = 0; i < 2015; i++){
            Block b = new Block(new ArrayList<>(), blockchain.getTopBlock().header.getHash(), pair.getPublic());
            b.header.difficultyBits = hexStringToByteArray("1b0404cb");
            b.header.time = current;
            current+=diff;
            blockchain.addBlock(b);
        }

        assertArrayEquals(hexStringToByteArray("1b038dee"), DifficultyAdjustment.getNextBlockBits(blockchain, 1295105167));
    }


}