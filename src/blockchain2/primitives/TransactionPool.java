package blockchain2.primitives;

import java.util.ArrayList;
import java.util.List;

public class TransactionPool {
    private List<Transaction> txns;
    private final int TRANSACTION_THRESHOLD;

    public TransactionPool(int threshold) {
        this.txns = new ArrayList<>();
        this.TRANSACTION_THRESHOLD = threshold;
    }

    public boolean addTransaction(Transaction txn) {
        this.txns.add(txn);
        return this.txns.size() >= TRANSACTION_THRESHOLD;
    }

    public boolean transactionExists(Transaction txn) {
        for(final Transaction t : this.txns) {
            if (t.equals(txn)) return true;
        }
        return false;
    }

    public void clear() {
        this.txns.clear();
    }

    public List<Transaction> getTxns() {
        return txns;
    }
}
