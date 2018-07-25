package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockNRBitcoinMessage extends Message {

    private Long blockNr;

    public BlockNRBitcoinMessage() {
        super();
    }

    public BlockNRBitcoinMessage(Long blockNr) {
        super();
        this.blockNr = blockNr;
    }

    public Long getBlockNr() {
        return blockNr;
    }

}
