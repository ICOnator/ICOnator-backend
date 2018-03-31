package io.iconator.monitor;

import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class MonitoringInit {

    private final static Logger LOG = LoggerFactory.getLogger(MonitoringInit.class);

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

    @PostConstruct
    private void init() throws Exception {
        try {
            initMonitors();
        } catch (HttpHostConnectException e) {
            LOG.error("Could not connect to ethereum full node on {}: {}",
                    appConfig.getEthereumNodeUrl(), e.getMessage());
            System.exit(1);
        }
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
