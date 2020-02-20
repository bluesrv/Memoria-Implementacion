package blockchain.Consensus;

public class Commit {
    public String blockHash;
    public String publicKey;
    public String signature;

    public Commit(String blockHash, String publicKey, String signature) {
        this.blockHash = blockHash;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public boolean equals(Commit commit) {
        return this.publicKey.equals(commit.publicKey);
    }
}
