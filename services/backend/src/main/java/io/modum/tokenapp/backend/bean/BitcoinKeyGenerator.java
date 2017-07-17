package io.modum.tokenapp.backend.bean;

import org.bitcoinj.core.ECKey;
import org.springframework.stereotype.Service;

@Service
public class BitcoinKeyGenerator {

    public Keys getKeys() {
        ECKey key = new ECKey();
        byte[] address = key.getPubKey();
        byte[] privateKey = key.getPrivKeyBytes();
        return new Keys().setAddress(address).setPrivateKey(privateKey);
    }

}
