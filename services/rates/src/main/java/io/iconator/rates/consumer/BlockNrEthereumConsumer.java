package io.iconator.rates.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.BlockNREthereumMessage;
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
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.BLOCK_NR_ETHEREUM_ROUTING_KEY;
import static java.util.Optional.ofNullable;

@Service
public class BlockNrEthereumConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BlockNrEthereumConsumer.class);

    public static final int HALF_HOUR = 1000 * 60 * 30;

    @Autowired
    private ObjectMapper objectMapper;

    private Long blockNr;
    private Long timestamp;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue,
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = BLOCK_NR_ETHEREUM_ROUTING_KEY)
    )
    public void receiveMessage(byte[] message) {
        LOG.debug("Received from consumer: " + new String(message));

        BlockNREthereumMessage blockNREthereumMessage = null;
        try {
            blockNREthereumMessage = objectMapper.reader().forType(BlockNREthereumMessage.class).readValue(message);
        } catch (IOException e) {
            LOG.error("Message not valid.");
        }

        try {
            Optional<BlockNREthereumMessage> optionalBlockNREthereumMessage = ofNullable(blockNREthereumMessage);
            optionalBlockNREthereumMessage.ifPresent((m) -> {
                blockNr = optionalBlockNREthereumMessage.get().getBlockNr();
                timestamp = optionalBlockNREthereumMessage.get().getTimestamp();
            });
        } catch (Exception e) {
            LOG.error("Error adding addresses to be monited by 'iconator-monitor'.", e);
        }
    }

    public BlockNrEthereumConsumer setValues(Long blockNr, Long timestamp) {
        this.blockNr = blockNr;
        this.timestamp = timestamp;
        return this;
    }

    public Long getCurrentBlockNr() {
        if(blockNr == null || timestamp == null) {
            LOG.error("No Ethereum block since startup");
            return null;
        }
        if(timestamp.longValue() + HALF_HOUR < new Date().getTime()) {
            LOG.error("Ethereum block over 30 minutes old, discarding");
            return null;
        }
        return blockNr;
    }

}
