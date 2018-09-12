package io.iconator.monitor.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinNet;
import io.iconator.commons.bitcoin.config.BitcoinConfig;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.monitor.BitcoinMonitor;
import io.iconator.monitor.EthereumMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.persistence.OptimisticLockException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Configuration
@Import(value = {MonitorAppConfigHolder.class, BitcoinConfig.class})
public class MonitorBean {

    private final static Logger LOG = LoggerFactory.getLogger(MonitorBean.class);

    @Autowired
    private MonitorAppConfigHolder appConfig;

    @Autowired
    private BitcoinConfig bitcoinConfig;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(appConfig.getEthereumNodeUrl()));
    }

    @Bean
    public NetworkParameters chainNetworkParameters() {
        return BitcoinNet.getNetworkParams(BitcoinNet.of(bitcoinConfig.getBitcoinNetwork()));
    }

    @Bean
    public Context bitcoinContext(NetworkParameters chainNetworkParameters) {
        return new Context(chainNetworkParameters);
    }

    @Bean
    public SPVBlockStore blockStore(NetworkParameters chainNetworkParameters)
            throws IOException, BlockStoreException {
        File blockStoreFile = Files.createTempFile("chain", "tmp").toFile();
        blockStoreFile.deleteOnExit();
        if (blockStoreFile.exists()) {
            blockStoreFile.delete();
        }
        return new SPVBlockStore(chainNetworkParameters, blockStoreFile);
    }

    @Bean
    public BlockChain bitcoinBlockchain(SPVBlockStore blockStore,
                                        Context bitcoinContext, NetworkParameters chainNetworkParameters)
            throws IOException, BlockStoreException {

        if (chainNetworkParameters.equals(MainNetParams.get())) {
            InputStream checkPoints = BitcoinMonitor.class.getClassLoader().getResourceAsStream("checkpoints.txt");
            CheckpointManager.checkpoint(chainNetworkParameters, checkPoints, blockStore, 1498867200L);
        } else if (chainNetworkParameters.equals(TestNet3Params.get())) {
            InputStream checkPoints = BitcoinMonitor.class.getClassLoader().getResourceAsStream("checkpoints-testnet.txt");
            CheckpointManager.checkpoint(chainNetworkParameters, checkPoints, blockStore, 1498867200L);
        }
        return new BlockChain(bitcoinContext, blockStore);
    }

    @Bean
    public PeerGroup peerGroup(BlockChain bitcoinBlockchain, Context bitcoinContext,
                               NetworkParameters chainNetworkParameters) {
        PeerGroup peerGroup = new PeerGroup(bitcoinContext, bitcoinBlockchain);
        // Regtest has no peer-to-peer functionality
        if (chainNetworkParameters.equals(MainNetParams.get())) {
            Stream.of(appConfig.getBitcoinNodePeerGroupSeed())
                    .forEach((peer) -> {
                        try {
                            peerGroup.addAddress(Inet4Address.getByName(peer));
                        } catch (UnknownHostException e) {
                            LOG.error("Not possible to add peer {} to the peer group. Unknown error: {}", peer, e);
                        }
                    });
        } else if (chainNetworkParameters.equals(TestNet3Params.get())) {
            peerGroup.addPeerDiscovery(new DnsDiscovery(chainNetworkParameters));
        }
        return peerGroup;
    }

    @Bean
    public EthereumMonitor ethereumMonitor(FxService fxService,
                                           Web3j web3j,
                                           PaymentLogService paymentLogService,
                                           MonitorService monitorService,
                                           ICOnatorMessageService messageService,
                                           InvestorService investorService,
                                           MonitorAppConfigHolder configHolder,
                                           Retryer retryer) {

        return new EthereumMonitor(fxService, paymentLogService, monitorService,
                messageService, investorService, web3j, configHolder, retryer);
    }

    @Bean
    public BitcoinMonitor bitcoinMonitor(FxService fxService,
                                         BlockChain bitcoinBlockchain,
                                         SPVBlockStore bitcoinBlockStore,
                                         Context bitcoinContext,
                                         NetworkParameters bitcoinNetworkParameters,
                                         PeerGroup peerGroup,
                                         PaymentLogService paymentLogService,
                                         MonitorService monitorService,
                                         ICOnatorMessageService messageService,
                                         InvestorService investorService,
                                         MonitorAppConfigHolder configHolder,
                                         Retryer retryer) {

        return new BitcoinMonitor(fxService, bitcoinBlockchain,
                bitcoinBlockStore, bitcoinContext, bitcoinNetworkParameters,
                peerGroup, paymentLogService, monitorService, messageService,
                investorService, configHolder, retryer);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Retryer<PaymentLog> retryer(MonitorAppConfigHolder configHolder) {
        return RetryerBuilder.<PaymentLog>newBuilder()
                .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                .retryIfExceptionOfType(OptimisticLockException.class)
                .withWaitStrategy(randomWait(
                        configHolder.getTokenConversionMaxTimeWait(), TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.neverStop())
                .build();
    }
}
