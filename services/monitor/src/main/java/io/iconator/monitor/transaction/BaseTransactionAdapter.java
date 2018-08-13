package io.iconator.monitor.transaction;

import io.iconator.commons.sql.dao.InvestorRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.MathContext;

public abstract class BaseTransactionAdapter implements TransactionAdapter {

    @Autowired
    private InvestorRepository investorRepository;

    @Override
    public BigDecimal getTransactionValueInMainUnit() {
        return new BigDecimal(getTransactionValue())
                .divide(getAtomicUnitFactor(), MathContext.DECIMAL128);
    }

    protected InvestorRepository getInvestorRepository() {
        return investorRepository;
    }

    public BigDecimal getAtomicUnitFactor() {
        return getCurrencyType().getAtomicUnitFactor();
    }
}
