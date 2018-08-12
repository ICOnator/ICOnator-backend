package io.iconator.rates.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import io.iconator.rates.service.EtherscanService;
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

    @Autowired
    private EtherscanService etherscanService;

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

    public Long getCurrentBlockNr() {
        if(blockNr == null || timestamp == null) {
            //fallback is API call to etherscan
            try {
                LOG.warn("No Ethereum block since startup");
                return etherscanService.getLatestEthereumHeight();
            } catch (IOException e) {
                LOG.error("Ethereum block height fallback failed - starting, discarding", e);
                return null;
            }
        }
        if(timestamp.longValue() + HALF_HOUR < new Date().getTime()) {
            //fallback is API call to etherscan
            try {
                LOG.warn("Ethereum block over 30 min old, using fallback");
                return etherscanService.getLatestEthereumHeight();
            } catch (IOException e) {
                LOG.error("Ethereum block height fallback failed, discarding", e);
                return null;
            }
        }
        return blockNr;
    }

}
