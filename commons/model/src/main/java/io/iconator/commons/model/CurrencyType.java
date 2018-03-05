package io.iconator.commons.model;

public enum CurrencyType {

    // Fiat:
    USD("USD"),
    CHF("CHF"),
    // Crypto Currency:
    ETH("ETH"),
    BTC("BTC"),
    ERC20("ERC20");

    private final String currencyType;

    CurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

}
