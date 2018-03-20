package io.iconator.commons.model;

public enum ExchangeType {

    KRAKEN("Kraken"),
    BITSTAMP("Bitstamp"),
    BITFINEX("Bitfinex"),
    GDAX("GDAX"),
    COINMARKETCAP("CoinMarketCap");

    private final String exchangeName;

    ExchangeType(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return this.exchangeName;
    }
}
