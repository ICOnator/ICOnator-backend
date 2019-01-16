package io.iconator.commons.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * Represents a registered investor participating in the ICO.
 */
@Entity
@Table(
        name = "investor",
        indexes = {
                @Index(columnList = "pay_in_bitcoin_address", name = "pay_in_bitcoin_address_idx"),
                @Index(columnList = "pay_in_ether_address", name = "pay_in_ether_address_idx")
        }
)
public class Investor {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * Used to verify the investors email address in the registration process.
     */
    @Column(name = "email_confirmation_token", unique = true, nullable = false)
    private String emailConfirmationToken;

    @Column(name = "wallet_address")
    private String walletAddress;

    /**
     * The Ether address to which the investor can make investments to.
     */
    @Column(name = "pay_in_ether_address", unique = true)
    private String payInEtherAddress;

    /**
     * The Bitcoin address to which the investor can make investments to.
     */
    @Column(name = "pay_in_bitcoin_address", unique = true)
    private String payInBitcoinAddress;

    /**
     * The investor's Ether address to which refunds can be send.
     */
    @Column(name = "refund_ether_address")
    private String refundEtherAddress;

    /**
     * The investor's Bitcoin address to which refunds can be send.
     */
    @Column(name = "refund_bitcoin_address")
    private String refundBitcoinAddress;

    /**
     * The IP address obtained from HTTP requests sent by the investor on registering for the ICO.
     */
    @Column(name = "ip_address")
    private String ipAddress;

    public Investor() {
    }

    public Investor(Date creationDate, String email, String emailConfirmationToken, String walletAddress, String payInEtherAddress, String payInBitcoinAddress, String refundEtherAddress, String refundBitcoinAddress, String ipAddress) {
        this.creationDate = creationDate;
        this.email = email;
        this.emailConfirmationToken = emailConfirmationToken;
        this.walletAddress = walletAddress;
        this.payInEtherAddress = payInEtherAddress;
        this.payInBitcoinAddress = payInBitcoinAddress;
        this.refundEtherAddress = refundEtherAddress;
        this.refundBitcoinAddress = refundBitcoinAddress;
        this.ipAddress = ipAddress;
    }

    public Investor(Date creationDate, String email, String emailConfirmationToken, String ipAddress) {
        this.creationDate = creationDate;
        this.email = email;
        this.emailConfirmationToken = emailConfirmationToken;
        this.ipAddress = ipAddress;
    }

    public Investor(Date creationDate, String email, String emailConfirmationToken) {
        this.creationDate = creationDate;
        this.email = email;
        this.emailConfirmationToken = emailConfirmationToken;
    }

    public long getId() {
        return id;
    }

    public Investor setId(long id) {
        this.id = id;
        return this;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Investor setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Investor setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getEmailConfirmationToken() {
        return emailConfirmationToken;
    }

    public Investor setEmailConfirmationToken(String emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
        return this;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public Investor setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
        return this;
    }

    public String getPayInEtherAddress() {
        return payInEtherAddress;
    }

    public Investor setPayInEtherAddress(String payInEtherAddress) {
        this.payInEtherAddress = payInEtherAddress;
        return this;
    }

    public String getPayInBitcoinAddress() {
        return payInBitcoinAddress;
    }

    public Investor setPayInBitcoinAddress(String payInBitcoinAddress) {
        this.payInBitcoinAddress = payInBitcoinAddress;
        return this;
    }

    public String getRefundEtherAddress() {
        return refundEtherAddress;
    }

    public Investor setRefundEtherAddress(String refundEtherAddress) {
        this.refundEtherAddress = refundEtherAddress;
        return this;
    }

    public String getRefundBitcoinAddress() {
        return refundBitcoinAddress;
    }

    public Investor setRefundBitcoinAddress(String refundBitcoinAddress) {
        this.refundBitcoinAddress = refundBitcoinAddress;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Investor setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Investor investor = (Investor) o;

        // TODO: is it enough?
        return Arrays.equals(email == null ? null : email.getBytes(),
                investor.getEmail() == null ? null : investor.getEmail().getBytes());
    }

    @Override
    public int hashCode() {
        // TODO: is it enough?
        return Objects.hash(email);
    }

}
