package blockchain2.device;

import blockchain2.primitives.Transaction;
import blockchain2.crypto.CryptoProvider;
import blockchain2.primitives.ProofOfRouting;
import core.DTNHost;
import lombok.Getter;
import lombok.Setter;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;

public class Device {
    private final Identity identity;
    @Getter private final CryptoProvider cryptoProvider;
    @Getter private final String id;
    @Getter @Setter private Integer reputation = 0;

    public Device (final Identity identity, final DTNHost host, final CryptoProvider cryptoProvider) {
        this.identity = identity;
        this.cryptoProvider = cryptoProvider;
        this.id = host.toString();
    }

    public Transaction createTransaction(final ProofOfRouting seed) throws SignatureException, InvalidKeyException {
        if (seed.getReceiverKey().equals(this.identity.getPublicKey()))
            return new Transaction(seed, this);
        return null;
    }

    public ProofOfRouting createProofOfRouting(final PublicKey receiverKey, final String receiverId) {
        return new ProofOfRouting(this.identity.getPublicKey(), receiverKey, this.id, receiverId);
    }

    public PublicKey getPublicKey() {
        return this.identity.getPublicKey();
    }

    public String sign(final String keyword) throws SignatureException, InvalidKeyException {
        return cryptoProvider.sign(this.identity.getPrivateKey(), keyword);
    }
}
