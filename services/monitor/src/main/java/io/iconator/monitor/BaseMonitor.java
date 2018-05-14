package io.iconator.monitor;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.Unit;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    protected EligibleForRefund eligibleForRefundInSatoshi(EligibleForRefund.RefundReason reason,
                                                        BigInteger amount,
                                                        String txoIdentifier,
                                                        Investor investor) {

        Unit unit = Unit.SATOSHI;
        CurrencyType currency = CurrencyType.BTC;
        EligibleForRefund entry =
                new EligibleForRefund(reason, amount, unit, currency, investor, txoIdentifier);

        return eligibleForRefundRepository.save(entry);
    }

    protected EligibleForRefund eligibleForRefundInWei(EligibleForRefund.RefundReason reason,
                                                           BigInteger amount,
                                                           String txoIdentifier,
                                                           Investor investor) {

        Unit unit = Unit.WEI;
        CurrencyType currency = CurrencyType.ETH;
        EligibleForRefund entry =
                new EligibleForRefund(reason, amount, unit, currency, investor, txoIdentifier);

        return eligibleForRefundRepository.save(entry);
    }
}
