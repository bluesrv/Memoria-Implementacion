package blockchain.Consensus;

import blockchain.main.Blockchain;
import blockchain.main.Device;
import blockchain.main.Transaction;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.SimClock;

import java.util.List;

import static applications.BlockchainApplication.APP_ID;

public abstract class ConcensusProtocol {

    protected TransactionPool txns;

    public ConcensusProtocol() { this.txns = new TransactionPool(); }

    public ConcensusProtocol(int txnsThreshold) {
        this.txns = new TransactionPool(txnsThreshold);
    }

    public boolean addTxn(Transaction txn, DTNHost host) {
        if (txns.transactionExists(txn) && !txn.validateTransaction()) return false;
        boolean thresholdReached = txns.addTransaction(txn);
        broadcast("TRANSACTION", txn, host);
        return thresholdReached;
    }

    public abstract void propose(DTNHost host, Device device, String previousHash);

    public void broadcast(String type, Object obj, DTNHost host) {
        List<Connection> cnxs = host.getConnections();
        for (Connection c : cnxs) {
            Message m = new Message(host, c.getOtherNode(host), "protocol" + type +
                    SimClock.getIntTime() + "-" + host.getAddress(),
                    1);
            m.addProperty("type", "protocol");
            m.addProperty(type, obj);
            m.setAppID(APP_ID);
            host.createNewMessage(m);
        }
    }

    public abstract void handle(Blockchain bc, Message msg, DTNHost host, Device device);

    public abstract void updateParticipants(Integer amm);
}
