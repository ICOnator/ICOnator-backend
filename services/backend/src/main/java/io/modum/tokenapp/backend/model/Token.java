package io.modum.tokenapp.backend.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "token", indexes = {
        @Index(columnList = "wallet_address", name = "wallet_address_idx"),
        @Index(columnList = "amount", name = "amount_idx")})
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "amount")
    private Integer amount;

    public Date getCreationDate() {
        return creationDate;
    }

    public Token setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public Token setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
        return this;
    }

    public Integer getAmount() {
        return amount;
    }

    public Token setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }
}
