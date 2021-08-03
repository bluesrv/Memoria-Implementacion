package blockchain2.consensus;

import blockchain2.primitives.Block;
import blockchain2.primitives.Transaction;
import core.Message;

public interface ConsensusProtocol {
    public void addTransaction(final Transaction txn);
    public Block handleInnerMessage(final Message message);
}