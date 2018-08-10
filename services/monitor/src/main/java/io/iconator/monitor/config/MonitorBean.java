package io.iconator.monitor.config;

import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinNet;
import io.iconator.commons.bitcoin.config.BitcoinConfig;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.BitcoinMonitor;
import io.iconator.monitor.EthereumMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenAllocationService;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;

@Configuration
@Import(value = {MonitorAppConfig.class, BitcoinConfig.class})
public class MonitorBean {

    @Autowired
    private MonitorAppConfig appConfig;

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
                               NetworkParameters chainNetworkParameters) throws UnknownHostException {
        PeerGroup peerGroup = new PeerGroup(bitcoinContext, bitcoinBlockchain);
        // Regtest has no peer-to-peer functionality
        if (chainNetworkParameters.equals(MainNetParams.get())) {
            // node-217.csg.uzh.ch.
            peerGroup.addAddress(Inet4Address.getByName("192.41.136.217"));
            // 212-51-140-183.fiber7.init7.net.
            peerGroup.addAddress(Inet4Address.getByName("212.51.140.183"));
            // 212-51-159-248.fiber7.init7.net.
            peerGroup.addAddress(Inet4Address.getByName("212.51.159.248"));
            // bitcoin.vable.ch.
            peerGroup.addAddress(Inet4Address.getByName("194.15.231.236"));
            // hosted-by.solarcom.ch.
            peerGroup.addAddress(Inet4Address.getByName("95.183.48.62"));
            // no reverse DNS
            peerGroup.addAddress(Inet4Address.getByName("193.234.225.156"));
            // yodel.dconnolly.com.
            peerGroup.addAddress(Inet4Address.getByName("176.9.154.110"));
            // static.109.19.9.5.clients.your-server.de.
            peerGroup.addAddress(Inet4Address.getByName("5.9.19.109"));

            // These are the DNS seeds found on the bitcoin core client:
            peerGroup.addAddress(Inet4Address.getByName("seed.bitcoin.sipa.be"));
            peerGroup.addAddress(Inet4Address.getByName("dnsseed.bluematt.me"));
            peerGroup.addAddress(Inet4Address.getByName("dnsseed.bitcoin.dashjr.org"));
            peerGroup.addAddress(Inet4Address.getByName("seed.bitcoinstats.com"));
            peerGroup.addAddress(Inet4Address.getByName("seed.bitcoin.jonasschnelli.ch"));
            peerGroup.addAddress(Inet4Address.getByName("seed.btc.petertodd.org"));
            peerGroup.addAddress(Inet4Address.getByName("seed.bitcoin.sprovoost.nl"));

        } else if (chainNetworkParameters.equals(TestNet3Params.get())) {
            peerGroup.addPeerDiscovery(new DnsDiscovery(chainNetworkParameters));
        }
        return peerGroup;
    }

    @Bean
    public EthereumMonitor ethereumMonitor(FxService fxService,
                                           Web3j web3j,
                                           InvestorRepository investorRepository,
                                           PaymentLogService paymentLogService,
                                           TokenAllocationService tokenAllocationService,
                                           EligibleForRefundService eligibleForRefundService,
                                           ICOnatorMessageService messageService) {

        return new EthereumMonitor(fxService, investorRepository, paymentLogService,
                tokenAllocationService, eligibleForRefundService, messageService, web3j);
    }

    @Bean
    public BitcoinMonitor bitcoinMonitor(FxService fxService,
                                         BlockChain bitcoinBlockchain,
                                         SPVBlockStore bitcoinBlockStore,
                                         Context bitcoinContext,
                                         NetworkParameters bitcoinNetworkParameters,
                                         PeerGroup peerGroup,
                                         InvestorRepository investorRepository,
                                         PaymentLogService paymentLogService,
                                         TokenAllocationService tokenAllocationService,
                                         EligibleForRefundService eligibleForRefundService,
                                         ICOnatorMessageService messageService) {
        return new BitcoinMonitor(fxService, bitcoinBlockchain,
                bitcoinBlockStore, bitcoinContext, bitcoinNetworkParameters, peerGroup,
                investorRepository, paymentLogService, tokenAllocationService,
                eligibleForRefundService, messageService);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
