package blockchain2.main;

import blockchain2.consensus.ConsensusProtocol;
import blockchain2.consensus.HoneyBadger.QueuingHoneyBadger;
import blockchain2.crypto.CryptoProvider;
import blockchain2.merkle.MerkleHash;
import blockchain2.primitives.Blockchain;
import blockchain2.primitives.Transaction;
import blockchain2.crypto.CryptoUtils;
import blockchain2.device.Device;
import blockchain2.exceptions.CorruptedProofOfRoutingException;
import blockchain2.primitives.ProofOfRouting;
import blockchain2.primitives.TransactionPool;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.SimClock;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.List;

import static applications.BlockchainApplication.APP_ID;

public class PolicyEngine {
    public static void handleRequest(final Message message,
                              final Device device,
                              final DTNHost appHost) throws SignatureException, CorruptedProofOfRoutingException, InvalidKeyException {
        final ProofOfRouting por = (ProofOfRouting) message.getProperty("por");
        final DTNHost messageReceiver = message.getTo();
        if (messageReceiver.equals(appHost)) {
            if (!isReceiverHost(por.getReceiverKey(), device)) {
                throw new CorruptedProofOfRoutingException();
            }
            Transaction txn = device.createTransaction(por);

            broadcastTransaction(txn, appHost);
        }
    }

    public static void handleTransaction(final Message message,
                                         final Device device,
                                         final DTNHost host,
                                         final ConsensusProtocol consensusProtocol) throws SignatureException, InvalidKeyException {
        final Transaction txn = (Transaction) message.getProperty("txn");
        final boolean shouldAdd = txn.validateTransaction(device.getCryptoProvider());
        if (shouldAdd) {
            broadcastTransaction(txn, host);
            consensusProtocol.addTransaction(txn);
        }
    }

    public static boolean validateIsNewTransaction(final Message message, final Blockchain blockchain){
        final Transaction transaction = (Transaction) message.getProperty("txn");
        if (transaction.getTimestamp() < blockchain.getBlockchain().get(0).getTimeStamp()) {
            return false;
        }
        final String txnHash = transaction.getTransactionId();
        return blockchain.getBlockchain()
                .stream()
                .noneMatch(
                        block -> block.getTxnList()
                                .stream()
                                .noneMatch(txn -> txn.getTransactionId().equals(txnHash))
                );
    }

    private static boolean isReceiverHost(final PublicKey receiverId, final Device device) {
        final String receiverStringId = CryptoUtils.bytesToString(receiverId.getEncoded());
        return receiverStringId.equalsIgnoreCase(device.getId());
    }

    private static void broadcastTransaction(final Transaction txn, final DTNHost host) {
        List<Connection> cnxs = host.getConnections();
        for (Connection c : cnxs) {
            Message m = new Message(host, c.getOtherNode(host), "txnBroadcast" +
                    SimClock.getIntTime() + "-" + host.getAddress(),
                    1);
            m.addProperty("type", "txnBradcast");
            m.addProperty("txn", txn);
            m.setAppID(APP_ID);
            host.createNewMessage(m);
        }
    }
}
