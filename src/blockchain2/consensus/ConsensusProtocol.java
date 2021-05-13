package blockchain2.consensus;

public interface ConsensusProtocol {
    public void handleTransactionThreshold();
    public void handleInnerMessage();
}
