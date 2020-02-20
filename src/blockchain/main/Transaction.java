package blockchain.main;

import java.security.PublicKey;
import java.util.LinkedHashMap;

public class Transaction {
    public String transactionId;
    public String publisherSignature;
    public String publisherKey;
    private String algorithm;
    private ProofOfRouting transmision;

    public LinkedHashMap<String, Integer> assignations;

    private static int sequence = 0;

    public Transaction(ProofOfRouting seed, Device device) {
        this.publisherKey = device.getPublicKey();
        this.transmision = seed;
        this.transactionId = this.calculateHash();
        this.publisherSignature = device.sign(this.transactionId);
        this.algorithm = device.getAlgorithm();
        this.assignations = new LinkedHashMap<>();
    }

    public boolean equals(Transaction txn) {
        return this.transactionId.equals(txn.transactionId);
    }

    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                this.transmision.getSenderId()
                + this.transmision.getReceiverId()
                + String.join("", this.transmision.getParticipantsKeys())
                + String.join("", this.transmision.getSignatures())
        );
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public boolean validateTransaction() {
        PublicKey publicKey = IdentityUtil.getPublicKey(publisherKey, algorithm);
        boolean validity = this.transmision.verify() && SignatureUtil.verify(publicKey, this.transactionId, this.publisherSignature.getBytes(), this.algorithm);
        if (!validity) return false;
        if(this.assignations.isEmpty())
            for (String id : this.transmision.getParticipantsId()) {
                assignations.put(id, 1);
            }
        return true;
    }

    public LinkedHashMap<String, Integer> getAssignations() {
        return this.assignations;
    }
}
