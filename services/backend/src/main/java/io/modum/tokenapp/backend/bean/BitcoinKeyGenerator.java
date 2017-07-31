package io.modum.tokenapp.backend.bean;

import io.modum.tokenapp.backend.utils.BitcoinNet;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BitcoinKeyGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinKeyGenerator.class);

    @Value("${modum.tokenapp.bitcoin.network}")
    private String bitcoinNetwork;

    public BitcoinKeyGenerator() {
    }

    public BitcoinKeyGenerator(String bitcoinNetwork) {
        this.bitcoinNetwork = bitcoinNetwork;
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
        } catch(AddressFormatException e) {
            return false;
        }
    }

    public boolean isValidAddress(byte[] address) {
        try {
            Address.fromP2SHHash(TestNet3Params.get(), address);
            return true;
        } catch(AddressFormatException e) {
            return false;
        }
    }

    public NetworkParameters getNetworkParameters() {
        return BitcoinNet.getNetworkParams(BitcoinNet.of(bitcoinNetwork));
    }

}
