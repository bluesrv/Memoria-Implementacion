package blockchain.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import blockchain.merkleTree.*;

public class Block {
    public String hash;
    public String previousHash;
    public ArrayList<Transaction> transactions;
    private MerkleTree txnHashTree; //our data will be a simple message.
    private MerkleNode root;
    private long timeStamp; //as number of milliseconds since 1/1/1970.

    //Block Constructor.
    public Block(ArrayList<Transaction> data, String previousHash) {
        this.transactions = data;
        this.txnHashTree = new MerkleTree();
        txnHashTree.appendLeaves(data
                .stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList())
                .toArray(new String[data.size()]));
        txnHashTree.buildTree();
        this.root = txnHashTree.getRoot();
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String calculatedHash = StringUtil.applySha256(StringUtil.hashToHex(
                MerkleHash.concatenate(
                        MerkleHash.concatenate(root.getHash().getValue(), Long.toString(timeStamp).getBytes()),
                        previousHash.getBytes()
                )
        ));
        return calculatedHash;
    }

    public String getValueHash() {
        String publicHash = StringUtil.hashToHex(root.getHash().getValue());
        return publicHash;
    }

    public boolean validateBlock() {
        boolean validity = true;

        List<MerkleProofHash> auditTrail;
        MerkleHash temp;
        for(Transaction txn : this.transactions) {
            temp = MerkleHash.createFromHash(txn.getTransactionId());
            auditTrail = this.txnHashTree.auditProof(temp);
            validity = validity && MerkleTree.verifyAudit(this.root.getHash(), temp, auditTrail);
            if (!validity) return false;
        }
        return true;
    }

    public boolean equals(Block b) {
        return b.hash.equals(this.hash);
    }
}