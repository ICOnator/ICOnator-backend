package io.iconator.commons.db.services;

import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EligibleForRefundService {

    @Autowired
    private EligibleForRefundRepository eligibleForRefundRepository;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public EligibleForRefund saveTransactionless(EligibleForRefund eligibleForRefund) {
        return eligibleForRefundRepository.save(eligibleForRefund);
    }

    @Transactional(readOnly = true)
    public boolean existsByTxIdentifier(String txIdentifier) {
        return eligibleForRefundRepository.existsByTxIdentifier(txIdentifier);
    }
}
