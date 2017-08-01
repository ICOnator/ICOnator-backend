package io.modum.tokenapp.backend.model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity(name = "investor")
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

    @Column(name = "email_confirmed")
    private Boolean emailConfirmed;

    @Column(name = "email_confirmation_token")
    private String emailConfirmationToken;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "payIn_ether_address", unique = true)
    private String payInEtherAddress;

    @Column(name = "payIn_ether_privateKey")
    private String payInEtherPrivateKey;

    @Column(name = "payIn_bitcoin_address", unique = true)
    private String payInBitcoinAddress;

    @Column(name = "payIn_bitcoin_privateKey")
    private String payInBitcoinPrivateKey;

    @Column(name = "refund_ether_address")
    private String refundEtherAddress;

    @Column(name = "refund_bitcoin_address")
    private String refundBitcoinAddress;


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

    public Boolean getEmailConfirmed() {
        return emailConfirmed;
    }

    public Investor setEmailConfirmed(Boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
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

    public String getPayInEtherPrivateKey() {
        return payInEtherPrivateKey;
    }

    public Investor setPayInEtherPrivateKey(String payInEtherPrivateKey) {
        this.payInEtherPrivateKey = payInEtherPrivateKey;
        return this;
    }

    public String getPayInBitcoinAddress() {
        return payInBitcoinAddress;
    }

    public Investor setPayInBitcoinAddress(String payInBitcoinAddress) {
        this.payInBitcoinAddress = payInBitcoinAddress;
        return this;
    }

    public String getPayInBitcoinPrivateKey() {
        return payInBitcoinPrivateKey;
    }

    public Investor setPayInBitcoinPrivateKey(String payInBitcoinPrivateKey) {
        this.payInBitcoinPrivateKey = payInBitcoinPrivateKey;
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
