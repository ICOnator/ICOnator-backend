package io.iconator.monitor.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

import java.util.HashMap;
import java.util.Map;

public class TestApplication {

    private final static Logger LOG = LoggerFactory.getLogger(TestApplication.class);

    private static String fullNodeAddress = "http://localhost:18545";

    private static Map<String, String> monitoredAddresses = new HashMap<String, String>()
    {{
        put("0x953ea1716540342ce3ce3474a1e24d7b862066bf", "");
    }};

    public static void main(String[] args) throws Exception {
        Web3j web3j = Web3j.build(new HttpService(fullNodeAddress));

        web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
                new DefaultBlockParameterNumber(1620498), true)
                .subscribe(block -> {

                    for (EthBlock.TransactionResult txr : block.getBlock().getTransactions()) {
                        EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txr.get();

                        //LOG.info("Transactions: " + tx.getHash());

                        if (tx.getHash().equals("0x72d108f9e1f08dd23df9d31cf8a0e954644c8944f89430247c7ecaa3b3cec3ad")) {
                            LOG.info("TransactionAdapter to address: " + tx.getTo());
                            LOG.info("Input: " + tx.getInput());
                        }

                        if (monitoredAddresses.get(tx.getTo()) != null) {
                            // Money was paid to a monitored address
                            try {
                                LOG.info("RECEIVED!");
                            } catch (Throwable e) {
                                LOG.error("Error in fundsReceived:", e);
                            }
                        }

                        if (monitoredAddresses.get(tx.getFrom().toLowerCase()) != null) {
                            // This should normally not happen as it means funds are stolen!
                            LOG.error("WARN: Removed: {} wei from payin address", tx.getValue().toString());
                        }
                    }
                },throwable -> {
                    LOG.error("Error during scanning of txs: ", throwable);
                });

    }

    public static void addMonitoredEtherAddress(String addressString) {
        if (!addressString.startsWith("0x"))
            addressString = "0x" + addressString;
        LOG.info("Add monitored Ethereum Address: {}", addressString);
        monitoredAddresses.put(addressString.toLowerCase(), addressString);
    }



}
