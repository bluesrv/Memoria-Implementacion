package blockchain.Consensus;

public class Prepare {
    public String blockHash;
    public String publicKey;
    public String signature;

    public Prepare(String blockHash, String publicKey, String signature) {
        this.blockHash = blockHash;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public boolean equals(Prepare prepare) {
        return this.publicKey.equals(prepare.publicKey);
    }
}
