package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockNRBitcoinMessage extends BlockNrMessage {

    public BlockNRBitcoinMessage() {
        super();
    }

    public BlockNRBitcoinMessage(Long blockNr, Long timestamp) {
        super(blockNr, timestamp);
    }
}
