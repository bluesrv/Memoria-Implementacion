package blockchain2.primitives;

import blockchain.merkleTree.MerkleNode;
import lombok.Getter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ResumeBlock {
    private final Map<String, Map<String, Integer>> heatMatrix;
    private String hash;
    private long timeStamp;

    public ResumeBlock() {
        this.heatMatrix = new HashMap<>();
        this.timeStamp = new Date().getTime();
    }

    public void updateResumeBlock(final List<Block> currentBlockchain) {
        currentBlockchain.forEach(
                block -> {
                    block.getTxnList().forEach(
                            transaction -> {
                                final ProofOfRouting transmision = transaction.getTransmision();
                                final List<String> sortedHosts = transmision.getParticipantsId();
                                updateCrossValue(transmision.getSenderId(), sortedHosts.get(0));
                                for(int i = 1; i < sortedHosts.size() - 1; i++) updateCrossValue(sortedHosts.get(i-1), sortedHosts.get(i));
                                updateCrossValue(sortedHosts.get(sortedHosts.size() - 1), transmision.getReceiverId());
                            }
                    );
                }
        );
        hash = currentBlockchain.get(currentBlockchain.size() - 1).getHash();
    }

    private void updateCrossValue(final String from, final String to) {
        if(!this.heatMatrix.containsKey(from)) this.heatMatrix.put(from, new HashMap<>());
        if(!this.heatMatrix.get(from).containsKey(to)) this.heatMatrix.get(from).put(to, 0);
        final Integer currentValue = this.heatMatrix.get(from).get(to);
        this.heatMatrix.get(from).put(to, currentValue + 1);
    }
}
