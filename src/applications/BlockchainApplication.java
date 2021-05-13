package applications;

import blockchain2.consensus.ConsensusProtocol;
import blockchain2.crypto.CryptoProvider;
import blockchain2.primitives.Blockchain;
import blockchain2.device.Device;
import blockchain2.device.Identity;
import blockchain2.device.IdentityFactory;
import blockchain2.exceptions.CorruptedProofOfRoutingException;
import blockchain2.main.PolicyEngine;
import blockchain2.primitives.TransactionPool;
import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Getter
@Setter
public class BlockchainApplication extends Application {
    public static final String BLOCKCHAIN_ENCRYPTOR = "encryptor";
    public static final String BLOCKCHAIN_PASSIVE = "passive";
    public static final String BLOCKCHAIN_INTERVAL = "interval";
    public static final String BLOCKCHAIN_OFFSET = "offset";
    public static final String BLOCKCHAIN_DEST_RANGE = "destinationRange";
    public static final String BLOCKCHAIN_SEED = "seed";
    public static final String BLOCKCHAIN_MSG_SIZE = "msgSize";
    public static final String BLOCKCHAIN_TXNS = "txnsThreshold";
    public static final String BLOCKCHAIN_BLOCKS = "blocksForGenesis";
    public static final String BLOCKCHAIN_VALIDATORS = "validatorPercentage";
    public static final String BLOCKCHAIN_PROTOCOL = "consensusProtocol";
    public static final String BLOCKCHAIN_MINAPPROVALS = "minApprovals";
    public static final String APP_ID = "fi.tkk.netlab.BlockchainApplication";

    private String	encryptor = "RSA";
    private double  lastMsg = 0;
    private double	interval = 500;
    private boolean passive = false;
    private int		seed = 0;
    private int		destMin = 0;
    private int		destMax = 1;
    private int		msgSize = 1;
    private int		txnsThreshold = 64;
    private int     blocks4Genesis = 10;
    private double  validatorPercentage = 0.3;
    private int     minApprovals = 7;
    private String  consensusProtocol = "HoneyBadgerBFT";
    private Random  rng;

    private static final Map<String, Integer> groupIntegrants = new HashMap<>();
    private static final Map<String, PublicKey> publicKeys = new HashMap<>();
    private ConsensusProtocol consensus;
    private Blockchain bc;
    private Device device;
    private CryptoProvider cryptoProvider;
    private IdentityFactory identityFactory;

    public BlockchainApplication(final Settings settings) throws NoSuchAlgorithmException {
        if (settings.contains(BLOCKCHAIN_PASSIVE)){
            this.passive = settings.getBoolean(BLOCKCHAIN_PASSIVE);
        }
        if (settings.contains(BLOCKCHAIN_INTERVAL)){
            this.interval = settings.getDouble(BLOCKCHAIN_INTERVAL);
        }
        if (settings.contains(BLOCKCHAIN_OFFSET)){
            this.lastMsg = settings.getDouble(BLOCKCHAIN_OFFSET);
        }
        if (settings.contains(BLOCKCHAIN_SEED)){
            this.seed = settings.getInt(BLOCKCHAIN_SEED);
        }
        if (settings.contains(BLOCKCHAIN_MSG_SIZE)) {
            this.msgSize = settings.getInt(BLOCKCHAIN_MSG_SIZE);
        }
        if (settings.contains(BLOCKCHAIN_DEST_RANGE)){
            int[] destination = settings.getCsvInts(BLOCKCHAIN_DEST_RANGE,2);
            this.destMin = destination[0];
            this.destMax = destination[1];
        }
        if (settings.contains(BLOCKCHAIN_TXNS)) {
            this.txnsThreshold = settings.getInt(BLOCKCHAIN_TXNS);
        }
        if (settings.contains(BLOCKCHAIN_BLOCKS)) {
            this.blocks4Genesis = settings.getInt(BLOCKCHAIN_BLOCKS);
        }
        if (settings.contains(BLOCKCHAIN_ENCRYPTOR)) {
            this.encryptor = settings.getSetting(BLOCKCHAIN_ENCRYPTOR);
        }
        if (settings.contains(BLOCKCHAIN_VALIDATORS)) {
            this.validatorPercentage = settings.getDouble(BLOCKCHAIN_VALIDATORS);
        }
        if (settings.contains(BLOCKCHAIN_PROTOCOL)) {
            this.consensusProtocol = settings.getSetting(BLOCKCHAIN_PROTOCOL);
        }
        if (settings.contains(BLOCKCHAIN_MINAPPROVALS)) {
            this.minApprovals = settings.getInt(BLOCKCHAIN_MINAPPROVALS);
        }
        this.rng = new Random(this.seed);
        this.bc = new Blockchain(this.blocks4Genesis, this.txnsThreshold);
        this.cryptoProvider = new CryptoProvider(this.encryptor);
        this.identityFactory = new IdentityFactory(cryptoProvider);
        super.setAppID(APP_ID);
    }

    public BlockchainApplication(final BlockchainApplication a) throws NoSuchAlgorithmException {
        super(a);
        this.lastMsg = a.getLastMsg();
        this.interval = a.getInterval();
        this.passive = a.isPassive();
        this.destMax = a.getDestMax();
        this.destMin = a.getDestMin();
        this.seed = a.getSeed();
        this.msgSize = a.getMsgSize();
        this.txnsThreshold = a.getTxnsThreshold();
        this.blocks4Genesis = a.getBlocks4Genesis();
        this.validatorPercentage = a.getValidatorPercentage();
        this.consensusProtocol = a.getConsensusProtocol();
        this.minApprovals = a.getMinApprovals();
        this.bc = a.getBc();
        this.cryptoProvider = a.getCryptoProvider();
        this.identityFactory = getIdentityFactory();
        this.rng = new Random(this.seed);
    }

    @SneakyThrows
    @Override
    public Message handle(final Message msg, final DTNHost host) {
        final String type = (String)msg.getProperty("type");
        if (type == null) {
            return msg;
        }
        if (device == null) {
            addNewDevice(host);
        }
        switch (type) {
            case "req":
                try {
                    PolicyEngine.handleRequest(msg, device, host);
                    super.sendEventToListeners("NewTxn", null, host);
                } catch (CorruptedProofOfRoutingException e) {
                    super.sendEventToListeners("CorruptPoR", e, host);
                }
                break;
            case "txnBroadcast":
                final boolean shouldInvokeConsensusProtocol = PolicyEngine.handleTransaction(msg,
                        this.device,
                        host,
                        this.bc.getTransactionPool());
                if(shouldInvokeConsensusProtocol) this.consensus.handleTransactionThreshold();
                break;
            case "protocol":
                consensus.handleInnerMessage();
                break;
        }

        return msg;
    }

    @Override
    @SneakyThrows
    public Application replicate() {
        return new BlockchainApplication(this);
    }

    @Override
    public void update(DTNHost host) {
        if (this.passive) return;
        if (device == null) addNewDevice(host);
        double curTime = SimClock.getTime();
        if (curTime - this.lastMsg >= this.interval) {
            // Time to send a new request;
            DTNHost rdHost = randomHost();
            Message m = new Message(host, rdHost, "req" +
                    SimClock.getIntTime() + "-" + host.getAddress(),
                    getMsgSize());
            m.addProperty("type", "req");
            m.addProperty("por", device.createProofOfRouting(publicKeys.get(rdHost.toString()), rdHost.toString()));
            m.setAppID(APP_ID);
            host.createNewMessage(m);

            // Call listeners
            super.sendEventToListeners("SentReq", null, host);

            this.lastMsg = curTime;
        }
    }

    private void addNewDevice(DTNHost host) {
        final Identity identity = identityFactory.generateNewIdentity();
        final Device device = new Device(identity, host, cryptoProvider);
        this.device = device;
        publicKeys.put(host.toString(), device.getPublicKey());
        final String groupId = getGroupId(host);
        final Integer count = (groupIntegrants.containsKey(groupId) ? groupIntegrants.get(groupId) : 0) + 1;
        groupIntegrants.put(groupId, count);
        if (consensusProtocol.equals("PBFT")) consensus = new PBFT(txnsThreshold, minApprovals, encryptor, count);
        else{
            consensus = new PBFT(count);
        }
    }

    private DTNHost randomHost() {
        int destaddr = 0;
        if (destMax == destMin) {
            destaddr = destMin;
        }
        destaddr = destMin + rng.nextInt(destMax - destMin);
        World w = SimScenario.getInstance().getWorld();
        return w.getNodeByAddress(destaddr);
    }

    private String getGroupId(DTNHost host) {
        return host.toString().substring(0, host.toString().length() - Integer.toString(host.getAddress()).length());
    }
}