package io.iconator.commons.db.services;

import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class PaymentLogService {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    public PaymentLog getPaymentLog(String transactionId) throws PaymentLogNotFoundException {
        return paymentLogRepository
                .findOptionalByTransactionId(transactionId)
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

    public PaymentLog updateProcessedDateAndSave(PaymentLog log) {
        log.setProcessedDate(new Date());
        return paymentLogRepository.saveAndFlush(log);
    }

}
