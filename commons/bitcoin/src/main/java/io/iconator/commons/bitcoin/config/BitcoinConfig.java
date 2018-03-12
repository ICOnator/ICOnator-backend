package io.iconator.commons.bitcoin.config;

import org.springframework.beans.factory.annotation.Value;

public class BitcoinConfig {

    @Value("${io.iconator.commons.bitcoin.network}")
    private String bitcoinNetwork;

    public String getBitcoinNetwork() {
        return bitcoinNetwork;
    }
}
