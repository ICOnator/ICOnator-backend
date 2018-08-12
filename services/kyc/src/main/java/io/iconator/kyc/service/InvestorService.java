package io.iconator.kyc.service;

import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
