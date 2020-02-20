package blockchain.Consensus;

public class InnerMessage {
    public String publicKey;
    public String message;
    public String signature;
    public String blockHash;

    public InnerMessage(String publicKey, String message, String signature, String blockHash) {
        this.blockHash = blockHash;
        this.publicKey = publicKey;
        this.message = message;
        this.signature = signature;
    }

    public boolean equals(InnerMessage innerMessage) {
        return this.publicKey.equals(innerMessage.publicKey);
    }
}
