package io.iconator.monitor.transaction;

import io.iconator.commons.model.CurrencyType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public interface TransactionAdapter {

    /**
     * The id which uniquely identifies the transaction can be appended with
     * more specific identification information. E.g. in the case of bitcoin
     * the index of the unspent transaction output can be added.
     * @return the id which uniquely identifies the transaction.
     */
    String getTransactionId();

    /**
     * @return the value of this transaction in the atomic unit of the currency
     * (e.g Wei for Ethereum transactions)
     */
    BigInteger getTransactionValue();

    /**
     * @return the value of this transaction in the main unit of the currency.
     * e.g. bitcoint
     */
    BigDecimal getTransactionValueInMainUnit();

    String getReceivingAddress();

    Long getAssociatedInvestorId();

    Long getBlockHeight();

    CurrencyType getCurrencyType();

    Date getBlockTime();

    String getWebLinkToTransaction();

    /**
     * The factor is the amount of atomic units that make up one main
     * unit of the currency encapsulated by this transaction.
     * @return the factor to convert atomic units to main units.
     */
    BigDecimal getAtomicUnitFactor();
}
