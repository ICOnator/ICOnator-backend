package io.iconator.commons.model;

public enum ExchangeType {

    KRAKEN("Kraken"),
    BITSTAMP("Bitstamp"),
    BITFINEX("Bitfinex");

    private final String exchangeName;

    ExchangeType(String exchangeName) {
        this.exchangeName = exchangeName;
    }

}
