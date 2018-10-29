package apis.static_structures;

import apis.domain.responses.GetOutputByAddressResponse;
import domain.transaction.Output;
import domain.utxo.UTXOIdentifier;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.codec.digest.DigestUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import script.OpCodes;
import script.ScriptExecutor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.*;

public class UTXO {

    private DB utxoDB;
    public HashSet<UTXOIdentifier> busy;

    public UTXO(DB utxoDB) {
        this.utxoDB = utxoDB;
        busy = new HashSet<>();
    }

    public synchronized Output get(UTXOIdentifier id) {
        return Output.fromBytes(utxoDB.get(id.serialize()));
    }

    public synchronized void put(UTXOIdentifier id, Output o) {
        utxoDB.put(id.serialize(), o.serialize());
    }

    public synchronized Output remove(UTXOIdentifier id) {
        if(!has(id))
            return null;

        Output o = Output.fromBytes(utxoDB.get(id.serialize()));
        utxoDB.delete(id.serialize());

        busy.remove(id);

        return o;
    }

    public synchronized void makeBusy(UTXOIdentifier id) {
        busy.add(id);
    }

    public synchronized void makeUnBusy(UTXOIdentifier id) {
        busy.remove(id);
    }

    public synchronized boolean has(UTXOIdentifier id) {
        byte[] data = utxoDB.get(id.serialize());
        return data != null && data.length != 0;
    }

    public synchronized Map<UTXOIdentifier, Output> getAll() {

        Map<UTXOIdentifier, Output> all = new HashMap<>();
        DBIterator iterator = utxoDB.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            all.put(UTXOIdentifier.fromBytes(iterator.peekNext().getKey()), Output.fromBytes(iterator.peekNext().getValue()));
        }

        try {
            iterator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return all;
    }

    public synchronized List<UTXOIdentifier> getAllByPublicKey(PublicKey pub) {
        List<UTXOIdentifier> matches = new ArrayList<>();
        byte[] ripmd160Address = Ripemd160.getHash(DigestUtils.sha256(pub.getEncoded()));
        Map<UTXOIdentifier, Output> all = getAll();

        all.entrySet().stream().forEach(entry->{
            ByteBuffer script = ByteBuffer.wrap(entry.getValue().scriptPubKey);

            if(ScriptExecutor.unsignedToBytes(script.get()) == OpCodes.OP_DUP && ScriptExecutor.unsignedToBytes(script.get()) == OpCodes.OP_HASH160) {
                script.get();
                script.getInt();

                byte[] pubAddressFromScript = new byte[20];
                script.get(pubAddressFromScript, 0, 20);

                if(ByteBuffer.wrap(pubAddressFromScript).equals(ByteBuffer.wrap(ripmd160Address))) {
                    matches.add(entry.getKey());
                }
            }
        });

        return matches;
    }

}
