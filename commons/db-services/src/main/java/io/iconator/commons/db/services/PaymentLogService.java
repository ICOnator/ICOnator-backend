package io.iconator.commons.db.services;

import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentLogService {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Transactional(readOnly = true)
    public boolean existsByTxIdentifier(String txIdentifier) {
        return paymentLogRepository.existsByTxIdentifier(txIdentifier);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public PaymentLog saveTransactionless(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    public PaymentLog save(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    public void delete(PaymentLog paymentLog) {
        paymentLogRepository.delete(paymentLog);
    }
}
