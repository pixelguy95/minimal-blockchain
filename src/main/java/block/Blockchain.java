package block;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static java.lang.Math.toIntExact;

@Deprecated
/**
 * See
 * apis.static_structures.Blockchain
 */
public class Blockchain {

    private Block[] blocks;

    Blockchain(){

    }


    private Block createGenesisBlocK() throws NoSuchAlgorithmException {

        String s = "JJ_AND_CJ_WAS_HERE_2018_10_02_UME";
        return new Block(0, s.getBytes(), getUnixTimestamp() );
    }

    /**
     * Needs to be casted to int to follow the Bitcoin reference implementation.
     */
    private static int getUnixTimestamp(){
        return toIntExact(System.currentTimeMillis() / 1000L);
    }

}
