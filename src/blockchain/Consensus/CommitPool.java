package blockchain.Consensus;

import blockchain.main.Device;
import blockchain.main.IdentityUtil;
import blockchain.main.SignatureUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class CommitPool {
    private HashMap<String, ArrayList<Commit>> commits;

    public CommitPool() {
        this.commits = new HashMap<>();
    }

    public Commit commit(Prepare prepare, Device user) {
        Commit commit = new Commit(prepare.blockHash, user.getPublicKey(), user.sign(prepare.blockHash));
        this.commits.putIfAbsent(prepare.blockHash, new ArrayList<>());
        this.commits.get(prepare.blockHash).add(commit);
        return commit;
    }

    public int addCommit(Commit commit) {
        this.commits.putIfAbsent(commit.blockHash, new ArrayList<>());
        this.commits.get(commit.blockHash).add(commit);
        return this.commits.get(commit.blockHash).size();
    }

    public boolean existingCommit(Commit commit) {
        for (Commit c : this.commits.get(commit.blockHash)) {
            if (c.equals(commit)) return true;
        }
        return false;
    }

    public boolean isValidCommit(Commit commit, String algorithm) {
        return SignatureUtil.verify(IdentityUtil.getPublicKey(commit.publicKey, algorithm), commit.blockHash, commit.signature.getBytes(), algorithm);
    }
}
