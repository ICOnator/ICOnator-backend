package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockNREthereumMessage extends Message {

    private Long blockNr;
    private Long timestamp;

    public BlockNREthereumMessage() {
        super();
    }

    public BlockNREthereumMessage(Long blockNr, Long timestamp) {
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
