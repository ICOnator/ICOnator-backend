package io.iconator.commons.bitcoin;

import io.iconator.commons.bitcoin.config.BitcoinConfig;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Import(BitcoinConfig.class)
public class BitcoinAddressService {

    private BitcoinConfig bitcoinConfig;

    @Autowired
    public BitcoinAddressService(BitcoinConfig bitcoinConfig) {
        this.bitcoinConfig = bitcoinConfig;
    }

    public boolean isValidBitcoinAddress(String address) {
        try {
            Address.fromBase58(getBitcoinNetworkParameters(), address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    private NetworkParameters getBitcoinNetworkParameters() {
        return BitcoinNet.getNetworkParams(BitcoinNet.of(bitcoinConfig.getBitcoinNetwork()));
    }

}
