package io.iconator.commons.db.services;

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

    public Optional<PaymentLog> getPaymentLog(String txIdentifier, CurrencyType currency) {
        return paymentLogRepository.findOptionalByTxIdentifierAndCurrency(txIdentifier, currency);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public PaymentLog saveImmediately(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }
}
