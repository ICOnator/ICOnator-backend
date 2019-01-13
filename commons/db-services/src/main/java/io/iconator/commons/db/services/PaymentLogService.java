package io.iconator.commons.db.services;

import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Provides common methods related to {@link PaymentLog} entity.
 * Acts as a facade to the {@link PaymentLogRepository}.
 */
@Service
public class PaymentLogService {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    /**
     * @param transactionId The transaction id for which to get the corresponding payment log.
     * @return the payment log corresponding to the the given transaction id.
     * @throws PaymentLogNotFoundException if no payment log exists in the database for the given
     * transaction id.
     */
    public PaymentLog getPaymentLog(String transactionId) throws PaymentLogNotFoundException {
        return paymentLogRepository
                .findOptionalByTransactionId(transactionId)
                .orElseThrow(PaymentLogNotFoundException::new);
    }

    /**
     * Inserts the given {@link PaymentLog} into the database or updates it if it already exists.
     * The save is executed in a new database transaction and commited after returning from this
     * method even if the calling code already runs in an active transaction.
     * @param log The payment log to insert/update.
     * @return the inserted/updated payment log.
     * @see CrudRepository#save(java.lang.Object)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentLog saveAndCommit(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    /**
     * Inserts the given {@link PaymentLog} into the database or updates it if it already exists.
     * Flushes changes to the database. The save is executed transactionless, which means that the
     * changes are commited to the database immediatly.
     * @param log The payment log to insert/update.
     * @return the inserted/updated payment log.
     * @see CrudRepository#save(java.lang.Object)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentLog saveTransactionless(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    /**
     * Inserts the given {@link PaymentLog} into the database or updates it if it already exists.
     * Flushes changes to the database. Transaction propagation is not specified.
     * @param log The payment log to insert/update.
     * @return the inserted/updated payment log.
     * @see CrudRepository#save(java.lang.Object)
     */
    public PaymentLog save(PaymentLog log) {
        return paymentLogRepository.saveAndFlush(log);
    }

    /**
     * Sets the payment log's {@link PaymentLog#processedDate} to the current date and updates
     * the payment log on the database. Flushes changes to the database. Transaction propagation is
     * not specified.
     * @param log The payment log to update.
     * @return the updated payment log.
     * @see CrudRepository#save(java.lang.Object)
     */
    public PaymentLog updateProcessedDateAndSave(PaymentLog log) {
        log.setProcessedDate(new Date());
        return paymentLogRepository.saveAndFlush(log);
    }

    /**
     * Checks if at least one paymen log exists for the given investor.
     * @param investorId The investor's id for which to check if a corresponding payment log exists.
     * @return true if the investor is connected to a payment log. False otherwise.
     */
    public boolean hasInvestorInvested(long investorId) {
        return paymentLogRepository.existsByInvestorId(investorId);
    }

}
