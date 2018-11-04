package apis.utils;

import apis.static_structures.Blockchain;
import apis.static_structures.TransactionPool;
import apis.static_structures.UTXO;
import apis.utils.validators.TransactionValidator;
import domain.block.Block;
import domain.block.StoredBlock;
import domain.utxo.UTXOIdentifier;
import node.Config;

import java.util.List;

public class BlockAddingManager {

    public static void addBlockAndManageUTXOs(Blockchain blockchain,
                                              UTXO utxo,
                                              TransactionPool transactionPool,
                                              TransactionValidator transactionValidator,
                                              Config config,
                                              Block block) {
        blockchain.addBlock(block);

        block.transactions.stream().forEach(t->{
            for(int i = 0; i < t.inputs.size(); i++) {
                utxo.makeBusy(new UTXOIdentifier(t.inputs.get(i).transactionHash, t.inputs.get(i).outputIndex));
            }

            transactionPool.remove(t.fullHash());
        });

        blockchain.getUTXOCandidates().entrySet().stream().forEach(entry-> utxo.put(entry.getKey(), entry.getValue()));
        blockchain.getUTXORemovalCandidates().keySet().stream().forEach(key -> utxo.remove(key));

        List<StoredBlock> pruned = blockchain.prune();
        pruned.stream().forEach(s-> {
            Block b = blockchain.getBlock(s.blockHeader.getHash());

            b.transactions.stream().forEach(t->{

                t.inputs.stream().forEach(input-> {
                    UTXOIdentifier utxoIdentifier = new UTXOIdentifier(input.transactionHash, input.outputIndex);
                    utxo.makeUnBusy(utxoIdentifier);
                });

                if(config.validateNewTransactions) {
                    if(transactionValidator.validate(t).passed) {
                        transactionPool.put(t);
                    }
                } else {
                    transactionPool.put(t);
                }
            });
        });
    }
}
