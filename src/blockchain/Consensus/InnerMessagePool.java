package blockchain.Consensus;

import blockchain.main.Device;
import blockchain.main.IdentityUtil;
import blockchain.main.SignatureUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class InnerMessagePool {
    private HashMap<String, ArrayList<InnerMessage>> messages;
    private String message;

    public InnerMessagePool() {
        this.messages = new HashMap<>();
        this.message = "INITIATE NEW ROUND";
    }

    public InnerMessage createMessage(String blockHash, Device user) {
        InnerMessage roundChange = new InnerMessage(user.getPublicKey(), this.message, user.sign(this.message + blockHash), blockHash);
        this.messages.putIfAbsent(blockHash, new ArrayList<>());
        this.messages.get(blockHash).add(roundChange);
        return roundChange;
    }

    public int addMessage(InnerMessage innerMessage) {
        this.messages.putIfAbsent(innerMessage.blockHash, new ArrayList<>());
        this.messages.get(innerMessage.blockHash).add(innerMessage);
        return this.messages.get(innerMessage.blockHash).size();
    }

    public boolean existingMessage(InnerMessage innerMessage) {
        for (InnerMessage m : this.messages.get(innerMessage.blockHash)) {
            if (m.equals(innerMessage)) return true;
        }
        return false;
    }

    public boolean isValidMessage(InnerMessage innerMessage, String algorithm) {
        return SignatureUtil.verify(IdentityUtil.getPublicKey(innerMessage.publicKey, algorithm), innerMessage.blockHash, innerMessage.signature.getBytes(), algorithm);
    }
}
