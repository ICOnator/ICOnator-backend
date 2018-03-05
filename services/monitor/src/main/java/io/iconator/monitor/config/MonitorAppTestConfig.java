package io.iconator.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class MonitorAppTestConfig {

    @Value("${io.iconator.monitor.eth.rinkeby.wallet-path}")
    private String ethWalletPath;

    @Value("${io.iconator.monitor.eth.rinkeby.wallet-password}")
    private String ethWalletPassword;

    @Value("${io.iconator.monitor.btc.testnet.wallet-path}")
    private String btcWalletPath;

    @Value("${io.iconator.monitor.btc.testnet.wallet-password}")
    private String btcWalletPassword;

    public String getEthWalletPath() {
        return ethWalletPath;
    }

    public String getEthWalletPassword() {
        return ethWalletPassword;
    }

    public String getBtcWalletPath() {
        return btcWalletPath;
    }

    public String getBtcWalletPassword() {
        return btcWalletPassword;
    }
}
