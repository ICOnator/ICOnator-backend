package io.iconator.commons.db.services;

import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// TODO [claude]: delete refund service
@Service
public class EligibleForRefundService {

    @Autowired
    private EligibleForRefundRepository eligibleForRefundRepository;

}
