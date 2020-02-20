package applications;

import blockchain.main.*;
import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;
import blockchain.Consensus.ConcensusProtocol;
import blockchain.Consensus.PBFT;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

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
    private String  consensusProtocol = "PBFT";
    private Random  rng;

    private static HashMap<String, Integer> groupIntegrants = new HashMap<>();
    private ConcensusProtocol consensus;
    private Blockchain bc;
    private Device device;

    public BlockchainApplication(Settings s) {
        if (s.contains(BLOCKCHAIN_PASSIVE)){
            this.passive = s.getBoolean(BLOCKCHAIN_PASSIVE);
        }
        if (s.contains(BLOCKCHAIN_INTERVAL)){
            this.interval = s.getDouble(BLOCKCHAIN_INTERVAL);
        }
        if (s.contains(BLOCKCHAIN_OFFSET)){
            this.lastMsg = s.getDouble(BLOCKCHAIN_OFFSET);
        }
        if (s.contains(BLOCKCHAIN_SEED)){
            this.seed = s.getInt(BLOCKCHAIN_SEED);
        }
        if (s.contains(BLOCKCHAIN_MSG_SIZE)) {
            this.msgSize = s.getInt(BLOCKCHAIN_MSG_SIZE);
        }
        if (s.contains(BLOCKCHAIN_DEST_RANGE)){
            int[] destination = s.getCsvInts(BLOCKCHAIN_DEST_RANGE,2);
            this.destMin = destination[0];
            this.destMax = destination[1];
        }
        if (s.contains(BLOCKCHAIN_TXNS)) {
            this.txnsThreshold = s.getInt(BLOCKCHAIN_TXNS);
        }
        if (s.contains(BLOCKCHAIN_BLOCKS)) {
            this.blocks4Genesis = s.getInt(BLOCKCHAIN_BLOCKS);
        }
        if (s.contains(BLOCKCHAIN_ENCRYPTOR)) {
            this.encryptor = s.getSetting(BLOCKCHAIN_ENCRYPTOR);
        }
        if (s.contains(BLOCKCHAIN_VALIDATORS)) {
            this.validatorPercentage = s.getDouble(BLOCKCHAIN_VALIDATORS);
        }
        if (s.contains(BLOCKCHAIN_PROTOCOL)) {
            this.consensusProtocol = s.getSetting(BLOCKCHAIN_PROTOCOL);
        }
        if (s.contains(BLOCKCHAIN_MINAPPROVALS)) {
            this.minApprovals = s.getInt(BLOCKCHAIN_MINAPPROVALS);
        }
        rng = new Random(this.seed);
        try {
            this.bc = new Blockchain();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setAppID(APP_ID);
    }

    public BlockchainApplication(BlockchainApplication a) {
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
        try {
            this.bc = new Blockchain();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.rng = new Random(this.seed);
    }

    private String getGroupId(DTNHost host) {
        return host.toString().substring(0, host.toString().length() - Integer.toString(host.getAddress()).length());
    }

    public void addNewDevice(DTNHost host) {
        try {
            Identity identity = new Identity(this.encryptor);
            Device device = new Device(host.toString(), identity);
            this.device = device;
            String groupId = getGroupId(host);
            Integer count = groupIntegrants.containsKey(groupId) ? groupIntegrants.get(groupId) : 0;
            groupIntegrants.put(groupId, ++count);
            if (consensusProtocol.equals("PBFT")) this.consensus = new PBFT(txnsThreshold, minApprovals, encryptor, count);
            else{
                this.consensus = new PBFT(count);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Initialized with RSA algorithm by default");
            Device device = new Device(host.toString());
            this.device = device;
            String groupId = getGroupId(host);
            Integer count = groupIntegrants.containsKey(groupId) ? groupIntegrants.get(groupId) : 0;
            groupIntegrants.put(groupId, count++);
            if (consensusProtocol.equals("PBFT")) this.consensus = new PBFT(txnsThreshold, minApprovals, encryptor, count);
            else{
                this.consensus = new PBFT(count);
            }
        }
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        String type = (String)msg.getProperty("type");
        if (type==null) return msg;
        if (device == null) addNewDevice(host);

        if (msg.getTo()!=host && type.equalsIgnoreCase("req")) {
            ProofOfRouting por = (ProofOfRouting)msg.getProperty("por");
            por.addRouter(device);
        }

        if (msg.getTo()==host && type.equalsIgnoreCase("req")) {
            ProofOfRouting por = (ProofOfRouting)msg.getProperty("por");
            if (!por.getReceiverId().equalsIgnoreCase(host.toString())) {
                    super.sendEventToListeners("CorruptPoR", null, host);
                return null;
            }
            Transaction txn = device.createTransaction(por);

            consensus.broadcast("TRANSACTION", txn, host);

            super.sendEventToListeners("NewTxn", null, host);
        }

        // Received a protocol message
        if (msg.getTo()==host && type.equalsIgnoreCase("protocol")) {
            if (consensus.getClass().getName().equals("PBFT")) consensus.updateParticipants(groupIntegrants.get(getGroupId(host)));
            consensus.handle(bc, msg, host, device);
        }

        if (msg.getTo()!=host && type.equalsIgnoreCase("protocol")) {
            return null;
        }

        return msg;
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

    @Override
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
            m.addProperty("por", device.createProofOfRouting(rdHost.toString()));
            m.setAppID(APP_ID);
            host.createNewMessage(m);

            // Call listeners
            super.sendEventToListeners("SentReq", null, host);

            this.lastMsg = curTime;
        }
    }

    public String getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(String algorithm) {
        this.encryptor = algorithm;
    }

    public double getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(double lastMsg) {
        this.lastMsg = lastMsg;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public int getDestMin() {
        return destMin;
    }

    public void setDestMin(int destMin) {
        this.destMin = destMin;
    }

    public int getDestMax() {
        return destMax;
    }

    public void setDestMax(int destMax) {
        this.destMax = destMax;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getTxnsThreshold() {
        return txnsThreshold;
    }

    public void setTxnsThreshold(int txnsThreshold) {
        this.txnsThreshold = txnsThreshold;
    }

    public int getBlocks4Genesis() {
        return blocks4Genesis;
    }

    public void setBlocks4Genesis(int blocks4Genesis) {
        this.blocks4Genesis = blocks4Genesis;
    }

    public int getMsgSize() {
        return msgSize;
    }

    public void setMsgSize(int msgSize) {
        this.msgSize = msgSize;
    }

    public double getValidatorPercentage() {
        return validatorPercentage;
    }

    public void setValidatorPercentage(double validatorPercentage) {
        this.validatorPercentage = validatorPercentage;
    }

    public String getConsensusProtocol() {
        return consensusProtocol;
    }

    public void setConsensusProtocol(String consensusProtocol) {
        this.consensusProtocol = consensusProtocol;
    }

    public int getMinApprovals() {
        return minApprovals;
    }

    public void setMinApprovals(int minApprovals) {
        this.minApprovals = minApprovals;
    }

    public Blockchain getBlockchain() {
        return bc;
    }

    public void setBlockchain(Blockchain bc) {
        this.bc = bc;
    }
}