package io.iconator.monitor;

import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class MonitoringInit {

    private final static Logger LOG = LoggerFactory.getLogger(MonitoringInit.class);

    /**
     * The maximum time in miliseconds to wait for a successful connection to the Ethereum full
     * node.
     */
    private final static int WAIT_FOR_ETH_MILLIS = 60000;
    /**
     * Time in miliseconds to wait between connection retries to the Ethereum full node.
     */
    private final static int WAIT_ETH_RETRY_MILLIS = 2000;
    /**
     * Number of connection retries to Ethereum full node before givin up.
     */
    private final static int NR_RETRIES = WAIT_FOR_ETH_MILLIS / WAIT_ETH_RETRY_MILLIS;

    @Autowired
    private MonitorAppConfigHolder appConfig;

    @Autowired
    private EthereumMonitor ethereumMonitor;

    @Autowired
    private BitcoinMonitor bitcoinMonitor;

    @Autowired
    private InvestorRepository investorRepository;

    /**
     * Starts the monitors (adds monitored addresses, starts monitoring of blockchains and
     * processing of transactionson) on initialization of the Spring Application Context.
     *
     * Monitors a started according to the setting of the application properties
     * {@link MonitorAppConfigHolder#bitcoinNodeEnabled} and
     * {@link MonitorAppConfigHolder#ethereumNodeEnabled}.
     *
     * If the Ethereum full node cannot be reached immediately connection retries are attempted.
     */
    @EventListener({ContextRefreshedEvent.class})
    void contextRefreshedEvent() {
        new Thread(() -> {
            for(int i=0;i<NR_RETRIES;i++)
                try {
                    initMonitors();
                    LOG.debug("started monitor successfully");
                    return; //everything is fine, exit thread
                } catch (Throwable t) {
                    LOG.info("Could not connect to ethereum full node on {}: {}. Trying again in {} msec.",
                            appConfig.getEthereumNodeUrl(), t.getMessage(), WAIT_ETH_RETRY_MILLIS);
                    try {
                        Thread.sleep(WAIT_ETH_RETRY_MILLIS);
                    } catch (InterruptedException e) {
                        //try hard
                    }
                }
            LOG.error("Could not connect to ethereum full node on {}. Giving up",
                    appConfig.getEthereumNodeUrl());
            //giving up, exit thread
        }).start();
    }

    /**
     * Adds all, so far, distributed payment addresses to the sets of monitored addresses of the
     * {@link BitcoinMonitor} and {@link EthereumMonitor} and starts the monitoring and processing
     * of transactions. Each monitor is only started if the application properties
     * {@link MonitorAppConfigHolder#bitcoinNodeEnabled} and
     * {@link MonitorAppConfigHolder#ethereumNodeEnabled} are set accordingly.
     */
    private void initMonitors() throws Exception {

        addExistinPaymentAdresses();

        if (appConfig.getEthereumNodeEnabled()) {
            ethereumMonitor.start();
            LOG.info("Ethereum monitor started.");
        }

        if (appConfig.getBitcoinNodeEnabled()) {
            bitcoinMonitor.start();
            LOG.info("Bitcoin monitor started.");
        }

    }

    /**
     * Retrieves all investors from the database and adds their payment addresses to the
     * {@link BitcoinMonitor} and {@link EthereumMonitor} instances respectively.
     * The timestamps provided with the addresses are the creation dates of the investors.
     */
    private void addExistinPaymentAdresses() {

        List<Investor> listInvestors = investorRepository.findAllByOrderByCreationDateAsc();

        listInvestors.stream().forEach((investor) -> {
            long timestamp = investor.getCreationDate().toInstant().getEpochSecond();

            ofNullable(investor.getPayInBitcoinAddress()).ifPresent((bitcoinAddress) -> {
                bitcoinMonitor.addPaymentAddressesForMonitoring(bitcoinAddress, timestamp);
            });

            ofNullable(investor.getPayInEtherAddress()).ifPresent((etherAddress) -> {
                ethereumMonitor.addPaymentAddressesForMonitoring(etherAddress, timestamp);
            });
        });

    }

}
