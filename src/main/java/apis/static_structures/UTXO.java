package apis.static_structures;

import domain.transaction.Output;
import domain.utxo.UTXOIdentifier;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UTXO {

    private DB utxoDB;

    public UTXO(DB utxoDB) {
        this.utxoDB = utxoDB;
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
        return o;
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

}
