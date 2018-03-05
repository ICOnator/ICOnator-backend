package io.iconator.commons.ethereum;

import io.iconator.commons.model.Keys;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

@Service
public class EthereumKeyGenerator {

    public Keys getKeys() {
        ECKey key = new ECKey();
        byte[] address = key.getAddress();
        byte[] privateKey = key.getPrivKeyBytes();
        return new Keys()
                .setAddress(address)
                .setAddressAsString("0x" + Hex.toHexString(address))
                .setPrivateKey(privateKey)
                .setAddressAsStringWithPrivate(key.toStringWithPrivate());
    }

    public boolean isValidAddress(String address) {
        try {
            return Utils.isValidAddress(Hex.decode(address.replace("0x", "")));
        } catch (DecoderException e) {
            return false;
        }
    }

    public boolean isValidAddress(byte[] address) {
        try {
            return Utils.isValidAddress(address);
        } catch (DecoderException e) {
            return false;
        }
    }

}
