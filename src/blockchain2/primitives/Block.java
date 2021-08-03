package blockchain2.primitives;

import blockchain2.crypto.CryptoUtils;
import blockchain2.merkle.MerkleHash;
import blockchain2.merkle.MerkleNode;
import blockchain2.merkle.MerkleProofHash;
import blockchain2.merkle.MerkleTree;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Block {
    private final String hash;
    private final String previousHash;
    private final List<Transaction> txnList;
    private final MerkleNode root;
    private final long timeStamp;

    public Block(final List<Transaction> txnList, final String previousHash) {
        this.txnList = txnList;
        this.root = buildTxnMerkleTree().getRoot();
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        final String calculatedHash = CryptoUtils.applySha256(
                CryptoUtils.hashToHex(
                        MerkleHash.concatenate(
                                MerkleHash.concatenate(root.getHash().getValue(), Long.toString(timeStamp).getBytes()),
                                previousHash.getBytes()
                        )
                )
        );
        return calculatedHash;
    }

    public boolean validateBlock() {
        final MerkleTree txnHashTree = buildTxnMerkleTree();
        return this.txnList.stream()
                .map(txn -> {
                    final MerkleHash temp = MerkleHash.createFromHash(txn.getTransactionId());
                    final List<MerkleProofHash> auditTrail = txnHashTree.auditProof(temp);
                    return MerkleTree.verifyAudit(this.root.getHash(), temp, auditTrail);
                })
                .reduce(true, (accumulator, actual) -> accumulator && actual);
    }

    private MerkleTree buildTxnMerkleTree() {
        final MerkleTree merkleTree = new MerkleTree();
        merkleTree.appendLeaves(
                this.txnList.stream()
                        .map(Transaction::getTransactionId)
                        .collect(Collectors.toList())
                        .toArray(new String[this.txnList.size()])
        );
        merkleTree.buildTree();
        return merkleTree;
    }
}
