package io.iconator.commons.db.services;

import io.iconator.commons.db.services.exception.RefundEntryAlreadyExistsException;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

/**
 * Provides common methods related to {@link EligibleForRefund} entity.
 * Acts as a facade to the {@link EligibleForRefundRepository}.
 */
@Service
public class EligibleForRefundService {

    private final static Logger LOG = LoggerFactory.getLogger(EligibleForRefund.class);

    @Autowired
    private EligibleForRefundRepository eligibleForRefundRepository;


    /**
     * Inserts the given {@link EligibleForRefund} into the database or updates it if it already
     * exists. Flushes changes to the database.
     * @param refundEntry the refund entry to insert/update.
     * @return the inserted/updated refund entry.
     * @throws RefundEntryAlreadyExistsException if a refund entry with the same transaction id as
     * in the given refund entry already exists.
     * @see CrudRepository#save(java.lang.Object)
     */
    public EligibleForRefund save(EligibleForRefund refundEntry)
            throws RefundEntryAlreadyExistsException {
        try {
            return eligibleForRefundRepository.saveAndFlush(refundEntry);
        } catch (DataIntegrityViolationException e) {
            throw new RefundEntryAlreadyExistsException();
        }
    }
}
