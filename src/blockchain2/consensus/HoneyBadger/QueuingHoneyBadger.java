package blockchain2.consensus.HoneyBadger;

import blockchain2.consensus.ConsensusProtocol;
import blockchain2.consensus.HoneyBadger.models.Contribution;
import blockchain2.consensus.HoneyBadger.models.Vote;
import blockchain2.device.Device;
import blockchain2.primitives.Block;
import blockchain2.primitives.Transaction;
import blockchain2.primitives.TransactionPool;
import core.DTNHost;
import core.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Getter
public class QueuingHoneyBadger implements ConsensusProtocol {
    private final TransactionPool txnPool;
    private final Device device;
    private final DTNHost host;
    private final Map<String, HoneyBadger> sessions;
    @Setter private int contributionSize;

    public QueuingHoneyBadger(final int txnThreshold, final int contributionSize, final Device device, final DTNHost host){
        this.txnPool = new TransactionPool(txnThreshold);
        this.sessions = new HashMap<>();
        this.contributionSize = contributionSize;
        this.device = device;
        this.host = host;
    }

    @Override
    public void addTransaction(Transaction txn) {
        if (!txnPool.transactionExists(txn)) {
            final boolean shouldCreateContribution = txnPool.addTransaction(txn);
            if (shouldCreateContribution) {
                final Contribution contribution = generateContribution(txnPool);
                final String sessionId = UUID.randomUUID().toString();
                final HoneyBadger session = new HoneyBadger();
                this.sessions.put(sessionId, session);
            }
        }
    }

    @Override
    public Block handleInnerMessage(final Message message) {
        final String phase = (String)message.getProperty("phase");
        final String sessionId = (String)message.getProperty("session");
        final HoneyBadger session = this.sessions.get(sessionId);
        Block generatedBlock = null;
        if (session == null) {
            return generatedBlock;
        }
        switch(phase) {
            case "contribution":
                final Contribution receivedContribution = (Contribution)message.getProperty("contribution");
                generatedBlock = session.receive(receivedContribution);
                break;
            case "vote":
                final Vote receivedVote = (Vote)message.getProperty("vote");
                generatedBlock = session.receive(receivedVote);
                break;
        }
        if (generatedBlock != null) {
            this.sessions.remove(sessionId);
        }
        return generatedBlock;
    }

    private Contribution generateContribution(final TransactionPool txnPool) {
        final List<Transaction> contributionList = new ArrayList<>();
        Collections.shuffle(txnPool.getTxns());
        contributionList.addAll(txnPool.getTxns().subList(0, contributionSize));
        return Contribution.builder()
                .contribution(contributionList)
                .contributionId(UUID.randomUUID().toString())
                .build();
    }
}
