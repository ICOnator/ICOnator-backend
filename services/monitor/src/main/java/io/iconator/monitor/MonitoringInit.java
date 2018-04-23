package io.iconator.monitor;

import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class MonitoringInit {

    private final static Logger LOG = LoggerFactory.getLogger(MonitoringInit.class);

    private final static int WAIT_FOR_ETH_MILLIS = 60000;
    private final static int WAIT_ETH_RETRY_MILLIS = 2000;
    private final static int NR_RETRIES = WAIT_FOR_ETH_MILLIS / WAIT_ETH_RETRY_MILLIS;

    @Autowired
    private MonitorAppConfig appConfig;

    @Autowired
    private EthereumMonitor ethereumMonitor;

    @Autowired
    private BitcoinMonitor bitcoinMonitor;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private ICOnatorMessageService messageService;

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

    private void initMonitors() throws Exception {

        monitorExistingAddresses();

        if (appConfig.getEthereumNodeEnabled()) {
            ethereumMonitor.start(appConfig.getEthereumNodeStartBlock());
            LOG.info("Ethereum monitor started.");
        }

        if (appConfig.getBitcoinNodeEnabled()) {
            bitcoinMonitor.start();
            LOG.info("Bitcoin monitor started.");
        }

    }

    private void monitorExistingAddresses() {

        List<Investor> listInvestors = investorRepository.findAllByOrderByCreationDateAsc();

        listInvestors.stream().forEach((investor) -> {
            long timestamp = investor.getCreationDate().getTime() / 1000L;

            ofNullable(investor.getPayInBitcoinPublicKey()).ifPresent((bitcoinPublicKey) -> {
                bitcoinMonitor.addMonitoredPublicKey(bitcoinPublicKey, timestamp);
            });

            ofNullable(investor.getPayInEtherPublicKey()).ifPresent((etherPublicKey) -> {
                ethereumMonitor.addMonitoredEtherPublicKey(etherPublicKey);
            });
        });

    }

}
