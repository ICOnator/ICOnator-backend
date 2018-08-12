package io.iconator.commons.cryptocompare.model;

public enum CryptoCompareCurrency {

    // Fiat:
    USD("USD"),
    CHF("CHF"),
    EUR("EUR"),

    // Crypto Currency:
    ETH("ETH"),
    BTC("BTC");

    private final String cryptoCompareCurrency;

    CryptoCompareCurrency(String cryptoCompareCurrency) {
        this.cryptoCompareCurrency = cryptoCompareCurrency;
    }

    public String getName() {
        return cryptoCompareCurrency;
    }

    public static boolean exists(String currencyString) {
        try {
            CryptoCompareCurrency.valueOf(currencyString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
