package io.iconator.monitor.transaction;

import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Blockchain-specific transaction adapters should extend this class when implementing the
 * {@link TransactionAdapter} interface because for example the
 * {@link TransactionAdapter#getAtomicUnitFactor()} should return the same factor independent of the
 * blockchain.
 */
public abstract class BaseTransactionAdapter implements TransactionAdapter {

    final private InvestorService investorService;

    public BaseTransactionAdapter(InvestorService investorService) {
        this.investorService = investorService;
    }

    @Override
    public BigDecimal getTransactionValueInMainUnit() throws MissingTransactionInformationException {
        try {
            return new BigDecimal(getTransactionValue())
                    .divide(getAtomicUnitFactor(), MathContext.DECIMAL128);
        } catch (MissingTransactionInformationException e) {
            throw e;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch transaction value in main unit.", e);
        }
    }

    protected InvestorService getInvestorService() {
        return investorService;
    }

    public BigDecimal getAtomicUnitFactor() {
        return getCurrencyType().getAtomicUnitFactor();
    }
}
