package io.iconator.commons.ethereum;

import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

@Service
public class EthereumAddressService {

    public boolean isValidEthereumAddress(String address) {
        try {
            return Utils.isValidAddress(Hex.decode(address.replace("0x", "")));
        } catch (Throwable e) {
            return false;
        }
    }

}
