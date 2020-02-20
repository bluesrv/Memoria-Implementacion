package blockchain.Consensus;

import blockchain.main.Block;
import blockchain.main.Blockchain;
import blockchain.main.Device;
import blockchain.main.Transaction;
import core.DTNHost;
import core.Message;

import java.util.Date;
import java.util.Random;

public class PBFT extends ConcensusProtocol {

    private BlockPool bp;
    private PreparePool pp;
    private CommitPool cp;
    private InnerMessagePool imp;
    private int minApprovals = 7;
    private String encryptor = "RSA";
    private int participants;
    private boolean isValidator = true;

    public PBFT(int participants) {
        this.bp = new BlockPool();
        this.pp = new PreparePool();
        this.cp = new CommitPool();
        this.imp = new InnerMessagePool();
        this.participants = participants;
        // determineValidator();
    }

    public PBFT(int threshold, int participants) {
        super(threshold);
        this.bp = new BlockPool();
        this.pp = new PreparePool();
        this.cp = new CommitPool();
        this.imp = new InnerMessagePool();
        this.participants = participants;
        // determineValidator();
    }

    public PBFT(int threshold, int minApprovals, String encryptor, int participants) {
        super(threshold);
        this.bp = new BlockPool();
        this.pp = new PreparePool();
        this.cp = new CommitPool();
        this.imp = new InnerMessagePool();
        this.minApprovals = minApprovals;
        this.encryptor = encryptor;
        this.participants = participants;
        // determineValidator();
    }

    @Override
    public void propose(DTNHost host, Device device, String previousHash) {
        Block newBlock = new Block(this.txns.getTxns(), previousHash);
        this.broadcast("PREPREPARE", newBlock, host);
    }

    @Override
    public void handle(Blockchain bc, Message msg, DTNHost host, Device device) {
        Transaction txn = (Transaction)msg.getProperty("TRANSACTION");
        Block prePrepare = (Block)msg.getProperty("PREPREPARE");
        Prepare prepare = (Prepare)msg.getProperty("PREPARE");
        Commit commit = (Commit)msg.getProperty("COMMIT");
        InnerMessage roundChange = (InnerMessage) msg.getProperty("ROUNDCHANGE");

        if (txn != null && isValidator) {
            boolean limitReached = addTxn(txn, host);
            if (limitReached) {
                String hash = "foo";
                if (bc.blockchain.size() != 0)
                    propose(host, device, bc.blockchain.get(bc.blockchain.size()-1).hash);
                else
                    propose(host, device, hash);
            }
        } else if (prePrepare != null) {
            if (!bp.blockExists(prePrepare) && prePrepare.validateBlock()) {
                bp.addBlock(prePrepare);
                broadcast("PREPREPARE", prePrepare, host);
                Prepare newPrepare = new Prepare(prePrepare.getValueHash(), device.getPublicKey(), device.sign(prePrepare.getValueHash()));
                broadcast("PREPARE", newPrepare, host);
            }
        } else if (prepare != null && isValidator) {
            if (!pp.existingPrepare(prepare) && pp.isValidPrepare(prepare, this.encryptor)) {
                int size = pp.addPrepare(prepare);
                broadcast("PREPARE", prepare, host);
                if (size >= minApprovals) {
                    Commit newCommit = new Commit(prepare.blockHash, device.getPublicKey(), device.sign(prepare.blockHash));
                    broadcast("COMMIT", newCommit, host);
                }
            }
        } else if (commit != null && isValidator) {
            if (!cp.existingCommit(commit) && cp.isValidCommit(commit, this.encryptor)) {
                int size = cp.addCommit(commit);
                broadcast("COMMIT", commit, host);
                if (size >= minApprovals) {
                    addApprovedBlock(bc, commit.blockHash);
                    InnerMessage newIM = new InnerMessage(device.getPublicKey(), "NEWROUND", device.sign(commit.blockHash), commit.blockHash);
                    broadcast("ROUNDCHANGE", newIM, host);
                }
            }
        } else if (roundChange != null && isValidator) {
            if (!imp.existingMessage(roundChange) && imp.isValidMessage(roundChange, this.encryptor)) {
                int size = imp.addMessage(roundChange);
                broadcast("ROUNDCHANGE", roundChange, host);
                if (size >= minApprovals) {
                    txns.clear();
                    // determineValidator();
                }
            }
        }
    }

    private void addApprovedBlock(Blockchain bc, String blockHash){
        Block newBlock = bp.getBlock(blockHash);
        bc.addBlock(newBlock);
    }

    public void determineValidator() {
        Random rng = new Random(new Date().getTime());
        int faultyNodes = (participants - 1)/3;
        int approvers = 2*faultyNodes + 1;
        this.isValidator = rng.nextInt(participants + 1) < approvers;
    }

    public boolean getIsValidator() {
        return isValidator;
    }

    @Override
    public void updateParticipants(Integer amm) {
        this.participants = amm;
    }
}
