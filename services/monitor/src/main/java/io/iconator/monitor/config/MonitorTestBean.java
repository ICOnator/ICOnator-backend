package io.iconator.monitor.config;

import io.iconator.monitor.service.BitcoinTestPaymentService;
import io.iconator.monitor.service.EthereumTestPaymentService;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.Web3j;

import java.io.IOException;

@Configuration
@Import(value = { MonitorAppTestConfig.class, MonitorBean.class })
@Profile("dev")
public class MonitorTestBean {

    @Autowired
    private MonitorAppTestConfig appTestConfig;

    @Bean
    public EthereumTestPaymentService ethereumTestPaymentService(Web3j web3j)
            throws IOException, CipherException {
        return new EthereumTestPaymentService(web3j, appTestConfig.getEthWalletPassword(), appTestConfig.getEthWalletPath());
    }

    @Bean
    public BitcoinTestPaymentService bitcoinTestPaymentService(BlockChain bitcoinBlockchain,
                                                               Context bitcoinContext,
                                                               NetworkParameters bitcoinNetworkParameters,
                                                               PeerGroup peerGroup)
            throws IOException, CipherException, UnreadableWalletException {
        return new BitcoinTestPaymentService(bitcoinBlockchain, bitcoinContext, bitcoinNetworkParameters, peerGroup,
                appTestConfig.getBtcWalletPassword(), appTestConfig.getBtcWalletPath());
    }

}
