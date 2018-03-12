package io.iconator.commons.bitcoin;

import io.iconator.commons.bitcoin.config.BitcoinConfig;
import io.iconator.commons.model.Keys;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

@Service
@Import(BitcoinConfig.class)
public class BitcoinKeyGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinKeyGenerator.class);

    private BitcoinConfig bitcoinConfig;

    @Autowired
    public BitcoinKeyGenerator(BitcoinConfig bitcoinConfig) {
        this.bitcoinConfig = bitcoinConfig;
    }

    public Keys getKeys() {

        ECKey key = new ECKey();
        byte[] address = key.getPubKey();
        String addressAsString = key.toAddress(getNetworkParameters()).toString();
        byte[] privateKey = key.getPrivKeyBytes();
        String addressAsStringWithPrivate = key.toStringWithPrivate(getNetworkParameters()).toString();
        return new Keys()
                .setAddress(address)
                .setAddressAsString(addressAsString)
                .setPrivateKey(privateKey)
                .setAddressAsStringWithPrivate(addressAsStringWithPrivate);
    }

    public boolean isValidAddress(String address) {
        try {
            Address.fromBase58(getNetworkParameters(), address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    public boolean isValidAddress(byte[] address) {
        try {
            Address.fromP2SHHash(TestNet3Params.get(), address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    public NetworkParameters getNetworkParameters() {
        return BitcoinNet.getNetworkParams(BitcoinNet.of(bitcoinConfig.getBitcoinNetwork()));
    }

}
