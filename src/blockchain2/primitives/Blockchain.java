package blockchain2.primitives;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class Blockchain {
    private final Integer resumeBlockThreshold;
    private final List<Block> blockchain;
    private final TransactionPool transactionPool;
    private final ResumeBlock resumeBlock;

    public Blockchain (final Integer resumeBlockThreshold, final Integer txnThreshold) {
        this.blockchain = new ArrayList<>();
        this.resumeBlockThreshold = resumeBlockThreshold;
        this.transactionPool = new TransactionPool(txnThreshold);
        this.resumeBlock = new ResumeBlock();
    }

    public Blockchain (final Blockchain blockchain) {
        this.blockchain = blockchain.getBlockchain();
        this.resumeBlockThreshold = blockchain.resumeBlockThreshold;
        this.resumeBlock = blockchain.resumeBlock;
        this.transactionPool = blockchain.transactionPool;
    }

    public void addBlock(final Block block) {
        this.blockchain.add(block);
        if (this.blockchain.size() == resumeBlockThreshold) {
            resumeBlock.updateResumeBlock(this.blockchain);
            this.blockchain.clear();
        }
    }

    public Boolean isChainValid() {
        //loop through blockchain to check hashes:
        if(resumeBlock.getHash() != null) {
            if(!resumeBlock.getHash().equals(blockchain.get(0).getHash())) return false;
        }
        for(int i=1; i < blockchain.size(); i++) {
            final Block currentBlock = blockchain.get(i);
            final Block previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true;
    }
}
