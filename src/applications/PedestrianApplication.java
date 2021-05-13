package applications;

import blockchain2.device.Device;
import blockchain2.device.Identity;
import blockchain2.device.IdentityFactory;
import blockchain2.primitives.ProofOfRouting;
import core.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class PedestrianApplication extends Application {

    public static final String PEDESTRIAN_ENCRYPTOR = "encryptor";
    public static final String APP_ID = "fi.tkk.netlab.PedestrianApplication";


    private static HashMap<String, Device> deviceHashMap = new HashMap<>();
    private String encryptor = "RSA";

    public PedestrianApplication(Settings s) {
        if (s.contains(PEDESTRIAN_ENCRYPTOR)) {
            this.encryptor = s.getSetting(PEDESTRIAN_ENCRYPTOR);
        }
        super.setAppID(APP_ID);
    }

    public PedestrianApplication(PedestrianApplication p) {
        super(p);
        this.encryptor = p.getEncryptor();
    }

    public void addNewDevice(DTNHost host) {
        try {
            Identity identity = new IdentityFactory()
                    .withAlgorithm(this.encryptor)
                    .getIdentity();
            Device device = new Device(host.toString(), identity);
            deviceHashMap.put(host.toString(), device);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Initialized with RSA algorithm by default");
            Device device = new Device(host.toString());
            deviceHashMap.put(host.toString(), device);
        }
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        String type = (String)msg.getProperty("type");
        if (type==null) return null;
        if (!deviceHashMap.containsKey(host.toString())) addNewDevice(host);

        Device device = deviceHashMap.get(host.toString());

        if (msg.getTo()!=host && type.equalsIgnoreCase("req")) {
            ProofOfRouting por = (ProofOfRouting)msg.getProperty("por");
            por.addRouter(device);
            return msg;
        }

        return null;
    }

    @Override
    public void update(DTNHost host) {
        if (!deviceHashMap.containsKey(host.toString())) addNewDevice(host);
    }

    @Override
    public Application replicate() {
        return new PedestrianApplication(this);
    }

    public String getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(String encryptor) {
        this.encryptor = encryptor;
    }
}
