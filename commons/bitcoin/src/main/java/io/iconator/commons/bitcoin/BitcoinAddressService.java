package io.iconator.commons.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BitcoinAddressService {

    @Value("${io.iconator.commons.bitcoin.network}")
    private String bitcoinNetwork;

    public String getBitcoinAddressFromPublicKey(String publicKey) {
        return ECKey.fromPublicOnly(Hex.decode(publicKey)).toAddress(getBitcoinNetworkParameters()).toString();
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
        return BitcoinNet.getNetworkParams(BitcoinNet.of(bitcoinNetwork));
    }

}
