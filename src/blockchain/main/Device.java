package blockchain.main;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Device {
    private Identity identity;
    private String id;
    private Integer reputation = 0;

    public Device() {
        byte[] array = new byte[128];
        new Random().nextBytes(array);
        String randomString = new String(array, Charset.forName("UTF-8"));
        MessageDigest digest = null;
        try {
            this.identity = new Identity("RSA");
            digest = MessageDigest.getInstance("SHA-256");
        } catch ( NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                randomString.getBytes(StandardCharsets.UTF_8));
        this.id = StringUtil.hashToHex(encodedhash);
    }

    public Device(Identity identity) {
        byte[] array = new byte[128];
        new Random().nextBytes(array);
        String randomString = new String(array, Charset.forName("UTF-8"));
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch ( NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                randomString.getBytes(StandardCharsets.UTF_8));
        this.id = StringUtil.hashToHex(encodedhash);
        this.identity = identity;
    }

    public Device(String id) {
        try {
            this.identity = new Identity("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.id = id;
    }

    public Device(String id, Identity identity) {
        this.identity = identity;
        this.id = id;
    }

    public Transaction createTransaction(ProofOfRouting seed) {
        if (seed.getReceiverId().equals(this.id))
            return new Transaction(seed, this);
        return null;
    }

    public ProofOfRouting createProofOfRouting(String receiverId) {
        return new ProofOfRouting(this.id, receiverId, this.getAlgorithm());
    }

    public String getPublicKey() {
        return this.identity.getFormatedPublicKey();
    }

    public String getAlgorithm() { return this.identity.getAlgorithm(); }

    public String sign(String keyword) {
        return SignatureUtil.sign(this.identity.getPrivateKey(), keyword, this.identity.getAlgorithm());
    }

    public String getId() {
        return id;
    }

}
