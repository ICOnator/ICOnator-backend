package io.iconator.rates.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.BLOCK_NR_ETHEREUM_ROUTING_KEY;
import static java.util.Optional.ofNullable;

/**
 * RabbitMQ Consumer that consumes messages holding a block number of the Ethereum
 * blockchain. It is assumed that a received block number corresponds to the latest block on the
 * chain. The latest block number and the block's time stamp are memorized until a new message is
 * received.
 */
@Service
public class BlockNrEthereumConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BlockNrEthereumConsumer.class);

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
    public void receiveMessage(BlockNREthereumMessage message) {
        try {
            Optional<BlockNREthereumMessage> optionalBlockNREthereumMessage = ofNullable(message);
            optionalBlockNREthereumMessage.ifPresent((ethereumMessage) -> {
                blockNr = ethereumMessage.getBlockNr();
                timestamp = ethereumMessage.getTimestamp();
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

    public Long getBlockNr() {
        return blockNr;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
