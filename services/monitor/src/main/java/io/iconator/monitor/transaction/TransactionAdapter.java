package io.iconator.monitor.transaction;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * This serves as a standardized interface to access information of transactions
 * from different blockchains. It encapsulates the specifics to fetch the
 * transaction information from different blockchains.
 * <p>
 * When implementing this interface make sure in every getter that possible
 * RuntimeExceptinos are handled and converted into
 * {@link MissingTransactionInformationException}s and that null values or empty strings
 * are checked and also signaled with {@link MissingTransactionInformationException}s.
 */
public interface TransactionAdapter {

    /**
     * The id which uniquely identifies the transaction can be appended with
     * more specific identification information. E.g. in the case of bitcoin
     * the index of the unspent transaction output can be added.
     *
     * @return the id which uniquely identifies the transaction.
     * @throws MissingTransactionInformationException if fetching the
     *                                                transaction id failed.
     */
    String getTransactionId() throws MissingTransactionInformationException;

    /**
     * @return the value of this transaction in the atomic unit of the currency
     * (e.g Wei for Ethereum transactions)
     * @throws MissingTransactionInformationException if fetching the
     *                                                transaction's value failed.
     */
    BigInteger getTransactionValue() throws MissingTransactionInformationException;

    /**
     * @return the value of this transaction in the main unit of the currency.
     * e.g. bitcoint
     * @throws MissingTransactionInformationException if fetching the
     *                                                transaction's value failed.
     */
    BigDecimal getTransactionValueInMainUnit() throws MissingTransactionInformationException;

    /**
     * @return the address to which the transaction is directed to.
     * @throws MissingTransactionInformationException if fetching the
     *                                                receiving address of this transaction failed.
     */
    String getReceivingAddress() throws MissingTransactionInformationException;

    /**
     * @return the investor which is registerd under the reciving address of
     * this transaction.
     * @throws MissingTransactionInformationException if fetching the
     *                                                investor failed.
     */
    Investor getAssociatedInvestor() throws MissingTransactionInformationException;

    /**
     * @return the block height (block number) of the block in which this
     * transaction is located.
     * @throws MissingTransactionInformationException if fetching the block
     *                                                height failed.
     */
    Long getBlockHeight() throws MissingTransactionInformationException;

    /**
     * @return the currency type of this transaction (e.g. BTC or ETH).
     */
    CurrencyType getCurrencyType();

    /**
     * @return the block time of the block in which this transaction is located (in milliseconds).
     * @throws MissingTransactionInformationException if fetching the block time
     *                                                failed.
     */
    Date getBlockTime() throws MissingTransactionInformationException;

    /**
     * The web link is usually composed by the URL of a blockchain info web page
     * appended with the transaction identifier of this transaction.
     *
     * @return the URL pointing to this transaction on an info web page.
     * @throws MissingTransactionInformationException if fetching the URL failed.
     */
    String getWebLinkToTransaction() throws MissingTransactionInformationException;

    /**
     * The factor is the amount of atomic units that make up one main
     * unit of the currency encapsulated by this transaction.
     *
     * @return the factor to convert atomic units to main units.
     */
    BigDecimal getAtomicUnitFactor();
}
