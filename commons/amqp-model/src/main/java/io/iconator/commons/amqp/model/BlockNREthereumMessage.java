package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockNREthereumMessage extends Message {

    private Long blockNr;

    public BlockNREthereumMessage() {
        super();
    }

    public BlockNREthereumMessage(Long blockNr) {
        super();
        this.blockNr = blockNr;
    }

    public Long getBlockNr() {
        return blockNr;
    }

}
