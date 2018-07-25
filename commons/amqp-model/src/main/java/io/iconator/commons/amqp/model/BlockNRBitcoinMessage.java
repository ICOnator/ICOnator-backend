package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockNRBitcoinMessage extends Message {

    private Long blockNr;
    private Long timestamp;

    public BlockNRBitcoinMessage() {
        super();
    }

    public BlockNRBitcoinMessage(Long blockNr, Long timestamp) {
        super();
        this.blockNr = blockNr;
        this.timestamp = timestamp;
    }

    public Long getBlockNr() {
        return blockNr;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
