package blockchain.main;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Identity {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String algorithm;

    public Identity(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(this.algorithm);
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public void setPrivateKeyString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedPrivateKey = stringToBytes(key);

        KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        this.privateKey = privateKey;
    }

    public void setPublicKeyString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException{

        byte[] encodedPublicKey = stringToBytes(key);

        KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        this.publicKey = publicKey;
    }

    public void saveToDiskPrivateKey(String path) throws IOException {
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            out.write(bytesToString(this.privateKey.getEncoded()));
            out.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void saveToDiskPublicKey(String path) {
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            out.write(bytesToString(this.publicKey.getEncoded()));
            out.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void openFromDiskPublicKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String content = this.readFileAsString(path);
        this.setPublicKeyString(content);
    }

    public void openFromDiskPrivateKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String content = this.readFileAsString(path);
        this.setPrivateKeyString(content);
    }

    public String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    public byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getAlgorithm() { return algorithm; }

    public String getFormatedPublicKey() {
        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }
}
