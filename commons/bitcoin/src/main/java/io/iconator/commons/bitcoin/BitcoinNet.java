package io.iconator.commons.bitcoin;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

public enum BitcoinNet {

    REGTEST, TESTNET, MAINNET;

    public static BitcoinNet of(String network) {
        String bitcoinNetLowerCase = network.toLowerCase();
        switch (bitcoinNetLowerCase) {
            case "regtest":
                return REGTEST;
            case "testnet":
                return TESTNET;
            case "mainnet":
                return MAINNET;
            default:
                throw new IllegalArgumentException("Invalid network " + network);
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static NetworkParameters getNetworkParams(BitcoinNet bitcoinNet) {
        switch (bitcoinNet) {
            case REGTEST:
                return RegTestParams.get();
            case TESTNET:
                return TestNet3Params.get();
            case MAINNET:
                return MainNetParams.get();
            default:
                throw new RuntimeException("Please set the server property io.iconator.commons.bitcoin.network to " +
                        "(regtest|testnet|main)");
        }
    }

}
