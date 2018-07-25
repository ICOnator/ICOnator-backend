package io.iconator.rates.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.BlockNRBitcoinMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.BLOCK_NR_BITCOIN_ROUTING_KEY;
import static java.util.Optional.ofNullable;

@Service
public class BlockNrBitcoinConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BlockNrBitcoinConsumer.class);

    public static final int TWO_HOURS = 1000 * 60 * 60 * 2;

    @Autowired
    private ObjectMapper objectMapper;

    private Long blockNr;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue,
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = BLOCK_NR_BITCOIN_ROUTING_KEY)
    )
    public void receiveMessage(byte[] message) {
        LOG.debug("Received from consumer: " + new String(message));

        BlockNRBitcoinMessage blockNRBitcoinMessage = null;
        try {
            blockNRBitcoinMessage = objectMapper.reader().forType(BlockNRBitcoinMessage.class).readValue(message);
        } catch (IOException e) {
            LOG.error("Message not valid.");
        }

        try {
            Optional<BlockNRBitcoinMessage> optionalBlockNRBitcoinMessage = ofNullable(blockNRBitcoinMessage);
            optionalBlockNRBitcoinMessage.ifPresent((m) -> {
                blockNr = optionalBlockNRBitcoinMessage.get().getBlockNr();
            });
        } catch (Exception e) {
            LOG.error("Error adding addresses to be monited by 'iconator-monitor'.", e);
        }
    }

    public BlockNrBitcoinConsumer setCurrentBlockNr(Long blockNr) {
        this.blockNr = blockNr;
        return this;
    }

    public Long getCurrentBlockNr() {
        if(blockNr == null) {
            LOG.error("No Bitcoin block since startup");
            return null;
        }
        if(blockNr.longValue() + TWO_HOURS < new Date().getTime()) {
            LOG.error("Bitcoin block over two hours old, discarding");
            return null;
        }
        return blockNr;
    }

}
