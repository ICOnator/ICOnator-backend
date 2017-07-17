package io.modum.tokenapp.backend.bean;

import org.ethereum.crypto.ECKey;
import org.springframework.stereotype.Service;

@Service
public class EthereumKeyGenerator {

    public Keys getKeys() {
        ECKey key = new ECKey();
        byte[] address = key.getAddress();
        byte[] privateKey = key.getPrivKeyBytes();
        return new Keys().setAddress(address).setPrivateKey(privateKey);
    }

}
