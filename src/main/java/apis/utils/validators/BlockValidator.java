package apis.utils.validators;


import apis.static_structures.Blockchain;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import domain.Validatable;
import domain.block.Block;
import domain.transaction.Transaction;
import node.Config;
import org.apache.commons.lang.SerializationUtils;
import utils.DifficultyAdjustmentRedux;
import utils.MerkleTreeUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BlockValidator implements Validator {

    public static final int MAX_BLOCK_SIZE = 1000000;

    private UTXO utxo;
    private Blockchain blockchain;
    private TransactionPool transactionPool;
    private Config config;
    private TransactionValidator transactionValidator;

    public BlockValidator(UTXO utxo, Blockchain blockchain, TransactionPool transactionPool, TransactionValidator transactionValidator, Config config) {
        this.utxo = utxo;
        this.blockchain = blockchain;
        this.transactionPool = transactionPool;
        this.transactionValidator = transactionValidator;
        this.config = config;
    }

    @Override
    public Result validate(Validatable v) {
        Block block = (Block)v;

        //Check if prev block exists
        if(!blockchain.getChain().containsKey(ByteBuffer.wrap(block.header.prevBlockHash))) {
            return new Result("Previous block not found!");
        }


        //Test if the block meets target
        long newBlockHeightIFAdded = blockchain.getChain().get(ByteBuffer.wrap(block.header.prevBlockHash)).height + 1;
        long correctBits = DifficultyAdjustmentRedux.getBlockBits(blockchain, blockchain.getBlock(block.header.prevBlockHash));

        if(block.header.bits != correctBits) {
            System.out.println(correctBits + " " + block.header.bits);
            return new Result("Faulty target difficulty!");
        }

        BigInteger target = DifficultyAdjustmentRedux.toTarget(correctBits);
        byte[] hash = block.header.getHash();
        if(new BigInteger(ByteBuffer.allocate(1+hash.length).put((byte) 0x00).put(hash).array()).compareTo(target) >= 0) {
            return new Result("Block did not reach target\n" + target.toString(16) + "\n" + new BigInteger(hash).toString(16));
        }


        //Check if timestamp is reasonable
        if(block.header.time < blockchain.getChain().get(ByteBuffer.wrap(block.header.prevBlockHash)).blockHeader.time)
            return new Result("This block seems to have traveled in time. The new timestamp must at least be larger than previous block");


        //Check that block size is smaller than the limit
        long calculatedBlockSize = 4 +
                block.header.serialize().array().length +
                8 +
                block.transactions.stream().mapToInt(t->t.serialize().length).sum() +
                block.coinbase.serialize().length;

        if(calculatedBlockSize > MAX_BLOCK_SIZE) {
            return new Result("The block was too large " + calculatedBlockSize + " bytes");
        }


        //Validate merkle tree
        byte[] calculatedMerkleTreeRoot = MerkleTreeUtils.getMerkleRootFromSerTxList(block.transactions.stream().map(t->t.serialize()).collect(Collectors.toList()));
        if(!Arrays.equals(calculatedMerkleTreeRoot, block.header.merkleRoot)) {
            return new Result("The merkle root did not match the one in the header. Someone might have tampered with the transaction list");
        }


        //Validate every transaction in the block
        long totalFees = 0;
        for(Transaction t : block.transactions) {
            Validator.Result result = transactionValidator.validate(t);

            totalFees += t.getFee(utxo);

            if(!result.passed) {
                return new Result("One of the transactions didn't pass validation with the following reason:\n" + result.resaon);
            }
        }


        //Validate the coinbase transaction
        long rewardForThisBlock = Block.INITIAL_REWARD / (long)(Math.pow(2.0, Math.floor(newBlockHeightIFAdded / 210_000)));
        if(block.coinbase.outputs.get(0).amount > (rewardForThisBlock+totalFees)) {
            return new Result("The coinbase transaction output amount exceeds the fees plus reward: " + block.coinbase.outputs.get(0).amount);
        }

        return new Result(); // VALIDATION PASSED
    }
}
