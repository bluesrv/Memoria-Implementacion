package blockchain.main;

import java.security.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SignatureUtil {
    public static String sign(PrivateKey privateKey, String keyword, String algorithm) {
        byte[] signature = null;
        String sign64 = null;
        String using = "SHA256with" + algorithm;
        try {
            Signature privateSignature = Signature.getInstance(using);
            privateSignature.initSign(privateKey);
            privateSignature.update(keyword.getBytes());
            signature = privateSignature.sign();
            sign64 = Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        return sign64;
    }

    public static boolean verify(PublicKey publicKey, String keyword, byte[] signature, String algorithm) {
        Signature publicSignature = null;
        String using = "SHA256with" + algorithm;
        try {
            publicSignature = Signature.getInstance(using);
            publicSignature.initVerify(publicKey);
            publicSignature.update(keyword.getBytes(UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            return publicSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }
}
