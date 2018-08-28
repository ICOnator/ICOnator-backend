package io.iconator.commons.db.services;

import io.iconator.commons.db.services.exception.RefundEntryAlreadyExistsException;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class EligibleForRefundService {

    private final static Logger LOG = LoggerFactory.getLogger(EligibleForRefund.class);

    @Autowired
    private EligibleForRefundRepository eligibleForRefundRepository;


    public EligibleForRefund save(EligibleForRefund refundEntry)
            throws RefundEntryAlreadyExistsException {
        try {
            return eligibleForRefundRepository.saveAndFlush(refundEntry);
        } catch (DataIntegrityViolationException e) {
            throw new RefundEntryAlreadyExistsException();
        }
    }
}
