package report;

import applications.BlockchainApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

public class BlockchainReporter extends Report implements ApplicationListener {

    private int corruptPoR = 0;
    private int transactions = 0;

    @Override
    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        if (!(app instanceof BlockchainApplication)) return;

        if (event.equalsIgnoreCase("CorruptPoR")) {
            corruptPoR++;
        }
        if (event.equalsIgnoreCase("NewTxn")) {
            transactions++;
        }
    }

    @Override
    public void done() {
        write("Results for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));

        String results = "Corrupted PoR: " + corruptPoR + "\n" +
                "Transactions created: " + transactions;

        write(results);
        super.done();
    }
}
