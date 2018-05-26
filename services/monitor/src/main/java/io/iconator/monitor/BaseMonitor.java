package io.iconator.monitor;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.Unit;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Component
public class BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BaseMonitor.class);

    protected final TokenConversionService tokenConversionService;
    protected final InvestorRepository investorRepository;
    protected final PaymentLogRepository paymentLogRepository;
    protected final EligibleForRefundRepository eligibleForRefundRepository;
    protected final FxService fxService;

    public BaseMonitor(TokenConversionService tokenConversionService,
                       InvestorRepository investorRepository,
                       PaymentLogRepository paymentLogRepository,
                       EligibleForRefundRepository eligibleForRefundRepository,
                       FxService fxService) {
        this.tokenConversionService = tokenConversionService;
        this.investorRepository = investorRepository;
        this.paymentLogRepository = paymentLogRepository;
        this.eligibleForRefundRepository = eligibleForRefundRepository;
        this.fxService = fxService;
    }

    protected boolean isTransactionUnprocessed(String txIdentifier) {
        return !paymentLogRepository.existsByTxIdentifier(txIdentifier)
            && !eligibleForRefundRepository.existsByTxIdentifier(txIdentifier);
    }

    protected void eligibleForRefund(BigInteger amount,
                                                  CurrencyType currencyType,
                                                  String txoIdentifier,
                                                  RefundReason reason,
                                                  Investor investor) {

        EligibleForRefund eligibleForRefund = new EligibleForRefund(reason, amount, currencyType, investor.getId(), txoIdentifier);
        try {
            LOG.info("Creating refund entry for transaction {}.", txoIdentifier);
            saveEligibleForRefund(eligibleForRefund);
        } catch (Exception e) {
            if (eligibleForRefundRepository.existsByTxIdentifier(txoIdentifier)) {
                LOG.info("Couldn't create refund entry because it already existed. " +
                        "I.e. transaction was already processed.", e);
            } else {
                LOG.error("Failed creating refund entry.", e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW)
    protected PaymentLog savePaymentLog(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    @Transactional(rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW)
    protected EligibleForRefund saveEligibleForRefund(EligibleForRefund eligibleForRefund) {
        return eligibleForRefundRepository.save(eligibleForRefund);
    }
}
