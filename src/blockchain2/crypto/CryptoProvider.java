package blockchain2.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoProvider {
    private final String using;
    private final Signature signatureProvider;
    private final KeyPairGenerator keyPairGenerator;

    public CryptoProvider(final String algorithm) throws NoSuchAlgorithmException {
        this.using = String.format("SHA256with%s", algorithm);
        this.signatureProvider = Signature.getInstance(using);
        this.keyPairGenerator = KeyPairGenerator.getInstance(using);
    }

    public String sign(final PrivateKey privateKey, final String keyword)
            throws InvalidKeyException, SignatureException {
        this.signatureProvider.initSign(privateKey);
        this.signatureProvider.update(keyword.getBytes());
        final byte[] signature = this.signatureProvider.sign();
        final String sign64 = Base64.getEncoder().encodeToString(signature);
        return sign64;
    }
    public boolean verify(final PublicKey publicKey, final String keyword, final byte[] signature)
            throws InvalidKeyException, SignatureException {
        this.signatureProvider.initVerify(publicKey);
        this.signatureProvider.update(keyword.getBytes(UTF_8));
        final byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return this.signatureProvider.verify(signatureBytes);
    }

    public KeyPair generateKeyPair() {
        this.keyPairGenerator.initialize(1024);
        return this.keyPairGenerator.generateKeyPair();
    }
}
