package io.iconator.commons.db.services;

import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Provides common methods related to {@link Investor} entity.
 * Acts as a facade to the {@link InvestorRepository}.
 */
@Service
public class InvestorService {
    private final Logger LOG = LoggerFactory.getLogger(InvestorService.class);

    private InvestorRepository investorRepository;

    @Autowired
    public InvestorService(InvestorRepository investorRepository) {
        assert investorRepository != null;
        this.investorRepository = investorRepository;
    }

    public Investor getInvestorByInvestorId(long investorId) throws InvestorNotFoundException {
        Optional<Investor> investorFromDb = investorRepository.findById(investorId);

        return investorFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public Investor getInvestorByEmail(String email) throws InvestorNotFoundException {
        Optional<Investor> investorFromDb = investorRepository.findOptionalByEmail(email);

        return investorFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public List<Investor> getAllInvestors() {
        return investorRepository.findAll();
    }

    public Investor getInvestorByBitcoinAddress(String address) throws InvestorNotFoundException {
        Optional<Investor> investorFromDb =
                investorRepository.findOptionalByPayInBitcoinAddress(address);

        return investorFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public Investor getInvestorByEthereumAddress(String address) throws InvestorNotFoundException {
        Optional<Investor> investorFromDb =
                investorRepository.findOptionalByPayInEtherAddressIgnoreCase(address);

        return investorFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Inserts the given {@link Investor} into the database or updates it if it already exists.
     * Flushes changes to the database. The save requires a new transaction. If the caller already has an open
     * transaciton, it gets suspended and a new transaction is opened. The new transaction is commited after leaving
     * this method.
     * @param investor The investor to insert/update.
     * @return the inserted/updated investor entry.
     * @see CrudRepository#save(java.lang.Object)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Investor saveRequireNewTransaction(Investor investor) {
        return investorRepository.saveAndFlush(investor);
    }
}
