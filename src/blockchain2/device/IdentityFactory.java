package blockchain2.device;

import blockchain2.crypto.CryptoProvider;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class IdentityFactory {
    private CryptoProvider cryptoProvider;

    public IdentityFactory(final CryptoProvider cryptoProvider) throws NoSuchAlgorithmException {
        this.cryptoProvider = cryptoProvider;
    }

    public Identity generateNewIdentity() {
        final KeyPair keyPair = cryptoProvider.generateKeyPair();
        return new Identity(keyPair.getPrivate(), keyPair.getPublic());
    }
}