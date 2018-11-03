package domain.block;

import apis.static_structures.Blockchain;
import apis.static_structures.UTXO;
import domain.transaction.CoinbaseTransaction;
import domain.transaction.Output;
import domain.transaction.Transaction;
import script.ScriptBuilder;
import utils.DifficultyAdjustmentRedux;
import utils.MerkleTreeUtils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockBuilder {

    public int blockSize;
    public BlockHeader header;
    public long transactionCounter;
    public List<Transaction> transactions;
    public CoinbaseTransaction coinbase;

    public BlockBuilder() {
        blockSize = 0;
        transactionCounter = 0;
        transactions = new ArrayList<>();
    }

    public BlockBuilder putTransactions(List<Transaction> transactions){
        this.transactions.addAll(transactions);
        this.transactionCounter+=transactions.size();
        return this;
    }

    public BlockBuilder generateCoinBase(PublicKey pub, long bestHeight, UTXO utxo) {
        long reward = Block.INITIAL_REWARD / (long)(Math.pow(2.0, Math.floor(bestHeight / 210_000)));
        long totalFees = transactions.stream().mapToLong(t->t.getFee(utxo)).sum();
        long amount = reward + totalFees;

        byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(pub);
        int scriptPubKeyLength = scriptPubKey.length;
        Output coinbaseOutput = new Output(amount, scriptPubKeyLength, scriptPubKey);
        this.coinbase = new CoinbaseTransaction(1, (short) 0, coinbaseOutput, null, 0xFFFFFFFF);

        return this;
    }

    public BlockBuilder generateCoinBaseWithoutFees(PublicKey pub, long bestHeight) {
        long reward = Block.INITIAL_REWARD / (long)(Math.pow(2.0, Math.floor(bestHeight / 210_000)));
        long amount = reward;

        byte[] scriptPubKey = ScriptBuilder.generateP2PKScript(pub);
        int scriptPubKeyLength = scriptPubKey.length;
        Output coinbaseOutput = new Output(amount, scriptPubKeyLength, scriptPubKey);
        this.coinbase = new CoinbaseTransaction(1, (short) 0, coinbaseOutput, null, 0xFFFFFFFF);

        return this;
    }

    public BlockBuilder generateHeader(Blockchain blockchain) {
        List<Transaction> transactionsPlusCoinBase = new ArrayList<>();
        transactionsPlusCoinBase.addAll(transactions);
        transactionsPlusCoinBase.add(coinbase);

        this.header = new BlockHeader(1,
                blockchain.getTopBlock().header.getHash(),
                MerkleTreeUtils.getMerkleRootFromSerTxList(transactions.stream().map(t->t.serialize()).collect(Collectors.toList())),
                DifficultyAdjustmentRedux.getNextBlockBits(blockchain));

        this.header.time = (System.currentTimeMillis() / 1000);
        return this;
    }

    public BlockBuilder generateHeaderWithGivenPrevBlock(Blockchain blockchain, byte[] prevBlockHash) {
        List<Transaction> transactionsPlusCoinBase = new ArrayList<>();
        transactionsPlusCoinBase.addAll(transactions);
        transactionsPlusCoinBase.add(coinbase);

        this.header = new BlockHeader(1,
                prevBlockHash,
                MerkleTreeUtils.getMerkleRootFromSerTxList(transactions.stream().map(t->t.serialize()).collect(Collectors.toList())),
                DifficultyAdjustmentRedux.getNextBlockBits(blockchain));

        this.header.time = 1541279724;
        return this;
    }

    public Block end() {
        int size = 4 +
                header.serialize().array().length +
                8 +
                transactions.stream().mapToInt(t->t.serialize().length).sum() +
                coinbase.serialize().length;

        return new Block(size, header, transactionCounter, transactions, coinbase);
    }
}
