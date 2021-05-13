package blockchain2.device;

import blockchain2.crypto.CryptoUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.security.PrivateKey;
import java.security.PublicKey;

@Getter
@EqualsAndHashCode
public class Identity {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Identity(final PrivateKey privateKey, final PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return CryptoUtils.bytesToString(this.publicKey.getEncoded());
    }
}
