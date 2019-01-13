package io.iconator.commons.db.services;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Provides common methods related to the {@link SaleTier} entity.
 * Acts as a facade to the {@link SaleTierRepository}.
 */
@Service
public class SaleTierService {

    @Autowired
    private SaleTierRepository saleTierRepository;

    /**
     * @return all sales tiers from in the database ordered by their start dates.
     */
    public List<SaleTier> getAllSaleTiersOrderByStartDate() {
        return saleTierRepository.findAllByOrderByStartDateAsc();
    }

    /**
     * @param tier The sale tier for which to get the subsequent tier.
     * @return the sale tier that has tier number equals to the tier number of the given tier
     * plus one.
     */
    public Optional<SaleTier> getSubsequentTier(SaleTier tier) {
        return saleTierRepository.findByTierNo(tier.getTierNo() + 1);
    }

    /**
     * @return The total amount of tokens (in the atomic unit) that was sold so far over all sale
     * tiers.
     */
    public BigInteger getTotalTomicsSold() {
        return saleTierRepository.findAll().stream()
                .map(SaleTier::getTomicsSold)
                .reduce(BigInteger::add)
                .orElse(BigInteger.ZERO);
    }

    /**
     * @param tier The sale tier for which to get all subsequent tiers.
     * @return all tiers that have a tier number greater than the given sale tier. Ordered by
     * ascending tier number.
     */
    public List<SaleTier> getAllSubsequentTiers(SaleTier tier) {
        return saleTierRepository.findTierByTierNoGreaterThanOrderByTierNoAsc(tier.getTierNo());
    }

    /**
     * @param date The date for which to find the active sale tier.
     * @return the sale tier which contains the given date in it's active date range (between start
     * and end date).
     */
    public Optional<SaleTier> getTierAtDate(Date date) {
        return saleTierRepository.findTierAtDate(date);
    }

    /**
     * Inserts the given {@link SaleTier} into the database or updates it if it already exists.
     * Flushes changes to the database. The save is executed transactionless, which means that the
     * changes are commited to the database immediatly.
     * @param tier The sale tier to insert/update.
     * @return the inserted/updated sale tier.
     * @see CrudRepository#save(java.lang.Object)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SaleTier saveTransactionless(SaleTier tier) {
        return saleTierRepository.saveAndFlush(tier);
    }
}
