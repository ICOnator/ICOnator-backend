package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockNrMessage extends Message {

    @JsonProperty("blockNr")
    private Long blockNr;

    @JsonProperty("timestamp")
    private Long timestamp;

    public BlockNrMessage() {
        super();
    }

    public BlockNrMessage(Long blockNr, Long timestamp) {
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
