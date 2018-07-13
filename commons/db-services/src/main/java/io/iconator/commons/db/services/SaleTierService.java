package io.iconator.commons.db.services;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
public class SaleTierService {

    private static final Logger LOG = LoggerFactory.getLogger(SaleTierService.class);

    @Autowired
    private SaleTierRepository saleTierRepository;

    public List<SaleTier> getAllSaleTiersOrderByStartDate() {
        return saleTierRepository.findAllByOrderByStartDateAsc();
    }

    public Optional<SaleTier> getNextTier(SaleTier tier) {
        return saleTierRepository.findByTierNo(tier.getTierNo() + 1);
    }

    public BigInteger getOverallTokenAmountSold() {
        return saleTierRepository.findAll().stream()
                .map(SaleTier::getTokensSold)
                .reduce(BigInteger::add)
                .orElse(BigInteger.ZERO);
    }


    public List<SaleTier> getAllFollowingTiers(SaleTier tier) {
        return saleTierRepository.findTierByTierNoGreaterThanOrderByTierNoAsc(tier.getTierNo());
    }
}
