package blockchain.Consensus;

import blockchain.main.Block;
import blockchain.main.Device;
import blockchain.main.IdentityUtil;
import blockchain.main.SignatureUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class PreparePool {
    private HashMap<String, ArrayList<Prepare>> prepared;

    public PreparePool() {
        this.prepared = new HashMap<>();
    }

    public Prepare prepare(Block block, Device user) {
        Prepare prepare = new Prepare(block.hash, user.getPublicKey(), user.sign(block.hash));
        this.prepared.putIfAbsent(block.hash, new ArrayList<>());
        this.prepared.get(block.hash).add(prepare);
        return prepare;
    }

    public int addPrepare(Prepare prepare) {
        this.prepared.putIfAbsent(prepare.blockHash, new ArrayList<>());
        this.prepared.get(prepare.blockHash).add(prepare);
        return this.prepared.get(prepare.blockHash).size();
    }

    public boolean existingPrepare(Prepare prepare) {
        for (Prepare p : this.prepared.get(prepare.blockHash)) {
            if (p.equals(prepare)) return true;
        }
        return false;
    }

    public boolean isValidPrepare(Prepare prepare, String algorithm) {
        return SignatureUtil.verify(IdentityUtil.getPublicKey(prepare.publicKey, algorithm), prepare.blockHash, prepare.signature.getBytes(), algorithm);
    }
}
