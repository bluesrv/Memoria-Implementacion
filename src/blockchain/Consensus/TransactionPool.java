package blockchain.Consensus;

import blockchain.main.Transaction;

import java.util.ArrayList;

public class TransactionPool {
    private ArrayList<Transaction> txns;
    private int TRANSACTION_THRESHOLD = 64;


    public TransactionPool() {
        this.txns = new ArrayList<>();
    }

    public TransactionPool(int threshold) {
        this.txns = new ArrayList<>();
        this.TRANSACTION_THRESHOLD = threshold;
    }

    public boolean addTransaction(Transaction txn) {
        this.txns.add(txn);
        return this.txns.size() >= TRANSACTION_THRESHOLD;
    }

    public boolean transactionExists(Transaction txn) {
        for(Transaction t : this.txns) {
            if (t.equals(txn)) return true;
        }
        return false;
    }

    public void clear() {
        this.txns.clear();
    }

    public ArrayList<Transaction> getTxns() {
        return txns;
    }
}
