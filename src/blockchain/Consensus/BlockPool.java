package blockchain.Consensus;

import blockchain.main.Block;

import java.util.ArrayList;

public class BlockPool {
    private ArrayList<Block> blocks;

    public BlockPool() {
        this.blocks = new ArrayList<>();
    }

    public void addBlock(Block block) {
        this.blocks.add(block);
    }

    public boolean blockExists(Block block) {
        for(Block b : this.blocks) {
            if (b.equals(block)) return true;
        }
        return false;
    }

    public Block getBlock(String hash) {
        for(Block b : this.blocks) {
            if (hash.equals(b.hash)) return b;
        }
        return null;
    }

    public void deleteBlock(Block block) {
        blocks.remove(block);
    }
}
