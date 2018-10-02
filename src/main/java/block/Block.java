package block;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class Block {

    private int nonce;
    private int index;
    private byte[] previousHash;
    private int timestamp;
    private byte[] header;
    private byte[] hash;

    Block(int index, byte[] previousHash, int timestamp) throws NoSuchAlgorithmException {

        this.index = index;
        this.previousHash = previousHash;
        this.timestamp = timestamp;


        ByteBuffer bb = ByteBuffer.allocate(36);

        this.header = new byte[36];
        bb.put(previousHash);
        bb.put(ByteBuffer.allocate(4).putInt(timestamp).array());
        this.header = bb.array();

        this.nonce = 0;

        this.hash = this.calcHash();
    }


    protected void mineBlock(int difficulty) throws NoSuchAlgorithmException {


        while(!Arrays.equals(Arrays.copyOfRange(this.hash, 0, difficulty), new byte[difficulty])){
            this.nonce++;
            this.hash = calcHash();
            if( nonce % 1000000 == 0 ){
                System.out.print(". ");
                if(nonce % 10000000 == 0){
                    System.out.println("");
                }
            }
        }
        System.out.println("BLOCK SUCCESSFULLY MINED WITH HASH: " + new String(this.hash));
    }

    private byte[] calcHash() throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(ByteBuffer.allocate(40).put(header).putInt(this.nonce).array());
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {

        byte[] prevHash = new byte[32];
        Random r = new Random();
        r.nextBytes(prevHash);

        new Block(1, prevHash, 1).mineBlock(4);
    }

}
