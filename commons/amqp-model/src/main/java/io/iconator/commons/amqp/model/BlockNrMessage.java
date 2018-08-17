package io.iconator.commons.amqp.model;

public class BlockNrMessage extends Message {

    private Long blockNr;
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
