package io.modum.tokenapp.backend.service;

import io.modum.tokenapp.backend.utils.BitcoinNet;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    @Value("${modum.tokenapp.bitcoin.network}")
    private String bitcoinNetwork;

    public String getBitcoinAddressFromPublicKey(String publicKey) {
        return ECKey.fromPublicOnly(Hex.decode(publicKey)).toAddress(getBitcoinNetworkParameters()).toString();
    }

    public String getEthereumAddressFromPublicKey(String publicKey) {
        return "0x" + Hex.toHexString(org.ethereum.crypto.ECKey.fromPublicOnly(Hex.decode(publicKey)).getAddress());
    }

    public boolean isValidEthereumAddress(String address) {
        try {
            return Utils.isValidAddress(Hex.decode(address.replace("0x", "")));
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean isValidBitcoinAddress(String address) {
        try {
            Address.fromBase58(getBitcoinNetworkParameters(), address);
            return true;
        } catch(AddressFormatException e) {
            return false;
        }
    }

    private NetworkParameters getBitcoinNetworkParameters() {
        return BitcoinNet.getNetworkParams(BitcoinNet.of(bitcoinNetwork));
    }
}
