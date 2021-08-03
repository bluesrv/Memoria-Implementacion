package blockchain2.primitives;

import blockchain2.crypto.CryptoProvider;
import blockchain2.crypto.CryptoUtils;
import blockchain2.device.Device;
import blockchain2.primitives.ProofOfRouting;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@EqualsAndHashCode
@Getter
public class Transaction {
    private final String transactionId;
    private final String publisherSignature;
    private final long timestamp;
    private final PublicKey publisherKey;
    private final ProofOfRouting transmision;

    private final Map<String, Integer> assignations;

    public Transaction(final ProofOfRouting seed, final Device device) throws SignatureException, InvalidKeyException {
        this.publisherKey = device.getPublicKey();
        this.transmision = seed;
        this.timestamp = new Date().getTime();
        this.transactionId = calculateHash();
        this.publisherSignature = device.sign(this.transactionId);
        this.assignations = new LinkedHashMap<>();
    }

    public boolean validateTransaction(final CryptoProvider cryptoProvider) throws SignatureException, InvalidKeyException {
        final PublicKey publicKey = publisherKey;
        final boolean validity = this.transmision.verify(cryptoProvider)
                && cryptoProvider.verify(publicKey, this.transactionId, this.publisherSignature.getBytes());
        if (!validity) return false;
        if(this.assignations.isEmpty())
            for (String id : this.transmision.getParticipantsId()) {
                assignations.put(id, 1);
            }
        return true;
    }

    private String calculateHash() {
        return CryptoUtils.applySha256(
                String.join("", this.transmision.getSignatures()) + String.valueOf(timestamp)
        );
    }
}
