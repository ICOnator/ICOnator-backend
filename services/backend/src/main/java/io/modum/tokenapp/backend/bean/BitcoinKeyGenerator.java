package io.modum.tokenapp.backend.bean;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

@Service
public class BitcoinKeyGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinKeyGenerator.class);

    public Keys getKeys() {
        ECKey key = new ECKey();
        byte[] address = key.getPubKey();
        String addressAsString = key.toAddress(MainNetParams.get()).toString();
        byte[] privateKey = key.getPrivKeyBytes();
        String addressAsStringWithPrivate = key.toStringWithPrivate(MainNetParams.get()).toString();
        return new Keys()
                .setAddress(address)
                .setAddressAsString(addressAsString)
                .setPrivateKey(privateKey)
                .setAddressAsStringWithPrivate(addressAsStringWithPrivate);
    }

    public boolean isValidAddress(String address) {
        try {
            // TODO:
            // give network parameters (prod net, unit test net, etc etc)
            Address.fromBase58(MainNetParams.get(), address);
            return true;
        } catch(AddressFormatException e) {
            return false;
        }
    }

    public boolean isValidAddress(byte[] address) {
        try {
            // TODO:
            // give network parameters (prod net, unit test net, etc etc)
            Address.fromP2SHHash(MainNetParams.get(), address);
            return true;
        } catch(AddressFormatException e) {
            return false;
        }
    }

}
