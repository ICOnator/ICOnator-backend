package io.iconator.commons.db.services;

import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentLogService {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Transactional(readOnly = true)
    public boolean exists(String txIdentifier, CurrencyType currency) {
        return paymentLogRepository.existsByTxIdentifierAndCurrency(txIdentifier, currency);
    }

    public PaymentLog getPaymentLogForUpdate(String txIdentifier, CurrencyType currency)
            throws PaymentLogNotFoundException {
        return paymentLogRepository
                .findOptionalByTxIdentifierAndCurrency(txIdentifier, currency)
                .orElseThrow(PaymentLogNotFoundException::new);
    }

    public PaymentLog getPaymentLogReadOnly(String txIdentifier, CurrencyType currency)
            throws PaymentLogNotFoundException {
        return paymentLogRepository
                .readOptionalByTxIdentifierAndCurrency(txIdentifier, currency)
                .orElseThrow(PaymentLogNotFoundException::new);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentLog saveAndCommit(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentLog saveTransactionless(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    public PaymentLog save(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

}
