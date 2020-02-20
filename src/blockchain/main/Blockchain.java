package blockchain.main;

import applications.BlockchainApplication;
import blockchain.Consensus.ConcensusProtocol;
import blockchain.Consensus.PBFT;
import core.DTNHost;
import core.Message;

import java.util.ArrayList;

public class Blockchain {

    public ArrayList<Block> blockchain;
    public Block currentGenesisBlock;

    public Blockchain () {
        this.blockchain = new ArrayList<>();
    }

    public void addBlock(Block block) {
        blockchain.add(block);
    }

    public Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true;
    }
}
