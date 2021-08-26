package blockchain2.primitives;

import java.util.ArrayList;
import java.util.List;

public class TransactionPool {
    private final List<Transaction> txns;

    public TransactionPool() {
        this.txns = new ArrayList<>();
    }

    public void addTransaction(final Transaction txn) {
        this.txns.add(txn);
    }

    public boolean transactionExists(final Transaction txn) {
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

    public void removeTransaction(final Transaction txn) {
        txns.remove(txn);
    }
}
