package io.modum.tokenapp.backend.model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "investor", indexes = {
        @Index(columnList = "pay_in_bitcoin_public_key", name = "pay_in_bitcoin_public_key_idx"),
        @Index(columnList = "pay_in_ether_public_key", name = "pay_in_ether_public_key_idx")})
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "email_confirmation_token", unique = true, nullable = false)
    private String emailConfirmationToken;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "pay_in_ether_public_key", unique = true)
    private String payInEtherPublicKey;

    @Column(name = "pay_in_bitcoin_public_key", unique = true)
    private String payInBitcoinPublicKey;

    @Column(name = "refund_ether_address")
    private String refundEtherAddress;

    @Column(name = "refund_bitcoin_address")
    private String refundBitcoinAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getPayInEtherPublicKey() {
        return payInEtherPublicKey;
    }

    public Investor setPayInEtherPublicKey(String payInEtherPublicKey) {
        this.payInEtherPublicKey = payInEtherPublicKey;
        return this;
    }

    public String getPayInBitcoinPublicKey() {
        return payInBitcoinPublicKey;
    }

    public Investor setPayInBitcoinPublicKey(String payInBitcoinPublicKey) {
        this.payInBitcoinPublicKey = payInBitcoinPublicKey;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Investor investor = (Investor) o;

        // TODO: is it enough?
        return Arrays.equals(email==null ? null : email.getBytes(),
                investor.getEmail()==null ? null : investor.getEmail().getBytes());
    }

    @Override
    public int hashCode() {
        // TODO: is it enough?
        return Objects.hash(Arrays.hashCode(email.toCharArray()));
    }

}
