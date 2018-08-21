package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockNREthereumMessage extends BlockNrMessage {

    public BlockNREthereumMessage() {
        super();
    }

    public BlockNREthereumMessage(Long blockNr, Long timestamp) {
        super(blockNr, timestamp);
    }
}
