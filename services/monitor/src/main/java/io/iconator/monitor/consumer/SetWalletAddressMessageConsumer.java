package io.iconator.monitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.SetWalletAddressMessage;
import io.iconator.monitor.BitcoinMonitor;
import io.iconator.monitor.EthereumMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.ADDRESS_SET_WALLET_ROUTING_KEY;
import static java.util.Optional.ofNullable;

/**
 * RabbitMQ consumer that consumes messages containing information about newly registered investors.
 * The payment addresses of a new investor are added to the {@link BitcoinMonitor} and
 * {@link EthereumMonitor} for monitoring.
 */
@Component
public class SetWalletAddressMessageConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SetWalletAddressMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EthereumMonitor ethereumMonitor;

    @Autowired
    private BitcoinMonitor bitcoinMonitor;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue(durable = "true"),
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = ADDRESS_SET_WALLET_ROUTING_KEY)
    )
    public void receiveMessage(SetWalletAddressMessage messageObject) {
        try {
            Optional<SetWalletAddressMessage> optionalSetWalletAddressMessage = ofNullable(messageObject);
            optionalSetWalletAddressMessage.ifPresent((m) -> {
                ofNullable(m.getInvestor()).ifPresent((investor) -> {
                    long timestamp = investor.getCreationDate().getTime();
                    bitcoinMonitor.addPaymentAddressesForMonitoring(investor.getPayInBitcoinAddress(), timestamp);
                    ethereumMonitor.addPaymentAddressesForMonitoring(investor.getPayInEtherAddress(), timestamp);
                });
            });
        } catch (Exception e) {
            LOG.error("Error adding addresses to be monited by 'iconator-monitor'.", e);
        }
    }

}
