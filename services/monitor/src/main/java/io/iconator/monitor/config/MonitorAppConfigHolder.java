package io.iconator.monitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Configuration
public class MonitorAppConfigHolder {

    private final static Logger LOG = LoggerFactory.getLogger(MonitorAppConfigHolder.class);

    @Value("${io.iconator.services.monitor.eth.node.enabled}")
    private Boolean ethereumNodeEnabled;

    @Value("${io.iconator.services.monitor.eth.node.start-block}")
    private Long ethereumNodeStartBlock;

    @Value("${io.iconator.services.monitor.eth.node.url}")
    private String ethereumNodeUrl;

    @Value("${io.iconator.services.monitor.eth.confirmation-blockdepth}")
    private Integer ethereumConfirmationBlockdepth;

    @Value("${io.iconator.services.monitor.btc.node.enabled}")
    private Boolean bitcoinNodeEnabled;

    @Value("${io.iconator.services.monitor.btc.node.peer-group-seed}")
    private String[] bitcoinNodePeerGroupSeed;

    @Value("${io.iconator.services.monitor.btc.node.fast-catch-up:#{null}}")
    private String bitcoinNodeFastCatchUp;

    @Value("${io.iconator.services.monitor.btc.confirmation-blockdepth}")
    private Integer bitcoinConfirmationBlockdepth;

    @Value("${io.iconator.services.monitor.retry.wait-between-attempts.max}")
    private Long tokenConversionMaxTimeWait;

    @Value("${io.iconator.services.monitor.token.fiat-base-per-token}")
    private BigDecimal fiatBasePerToken;

    @Value("${io.iconator.services.monitor.token.total-amount}")
    private BigDecimal totalTokenAmount;

    @Value("${io.iconator.services.monitor.token.atomic-unit-factor}")
    private Integer atomicUnitFactor;

    @Value("${io.iconator.services.monitor.fiat-base.payment.min}")
    private BigDecimal fiatBasePaymentMinimum;

    /**
     * Sets the time in miliseconds which is used to determine if a transaction might still
     * be processed by a monitor instance. If the last modified date of a payment log is within
     * this time it is assumed that the correspoding transaction is still being processed.
     */
    @Value("${io.iconator.services.monitor.transaction-processing-time}")
    private Integer transactionProcessingTime;

    public Boolean getEthereumNodeEnabled() {
        return ethereumNodeEnabled;
    }

    public Long getEthereumNodeStartBlock() {
        return ethereumNodeStartBlock;
    }

    public String getEthereumNodeUrl() {
        return ethereumNodeUrl;
    }

    public Boolean getBitcoinNodeEnabled() {
        return bitcoinNodeEnabled;
    }

    public String[] getBitcoinNodePeerGroupSeed() {
        return bitcoinNodePeerGroupSeed;
    }

    public Optional<Instant> getBitcoinNodeFastCatchUp() {
        if (bitcoinNodeFastCatchUp == null) {
            return Optional.empty();
        }
        try {
            Instant fastCatchUpInstant = LocalDateTime
                    .parse(bitcoinNodeFastCatchUp, DateTimeFormatter.ISO_DATE_TIME)
                    .toInstant(ZoneOffset.UTC);
            return Optional.of(fastCatchUpInstant);
        } catch (DateTimeParseException e) {
            LOG.error("Parameter bitcoinNodeFastCatchUp not set due to an error while parsing the date.", e);
            return Optional.empty();
        }
    }

    public Long getTokenConversionMaxTimeWait() {
        return tokenConversionMaxTimeWait;
    }

    public BigDecimal getFiatBasePerToken() {
        return fiatBasePerToken;
    }

    public BigDecimal getTotalTokenAmount() {
        return totalTokenAmount;
    }

    public BigInteger getAtomicUnitFactor() {
        return BigInteger.TEN.pow(atomicUnitFactor);
    }

    public Integer getEthereumConfirmationBlockdepth() {
        return ethereumConfirmationBlockdepth;
    }

    public Integer getBitcoinConfirmationBlockdepth() {
        return bitcoinConfirmationBlockdepth;
    }

    public BigDecimal getFiatBasePaymentMinimum() {
        return fiatBasePaymentMinimum;
    }

    public Integer getTransactionProcessingTime() {
        return transactionProcessingTime;
    }
}
