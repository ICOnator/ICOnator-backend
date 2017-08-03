package io.modum.tokenapp.minting.model;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * Created by draft on 03.08.17.
 */

@Entity
@Table(name = "payin", indexes = {
        @Index(columnList = "wallet_address", name = "wallet_address2_idx"),
        @Index(columnList = "time", name = "time_idx")})
public class Payin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Temporal(TIMESTAMP)
    @Column(name = "time", nullable = false)
    private Date time;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "wei")
    private Long wei;

    @Column(name = "satoshi")
    private Long satoshi;

    @Column(name = "block_nr_eth")
    private Long blockNrEth;

    @Column(name = "block_nr_btc")
    private Long blockNrBtc;

    public Date getCreationDate() {
        return creationDate;
    }

    public Payin setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public Payin setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public Payin setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
        return this;
    }

    public Long getWei() {
        return wei;
    }

    public Payin setWei(Long wei) {
        this.wei = wei;
        return this;
    }

    public Long getSatoshi() {
        return satoshi;
    }

    public Payin setSatoshi(Long satoshi) {
        this.satoshi = satoshi;
        return this;
    }

    public Long getBlockNrEth() {
        return blockNrEth;
    }

    public Payin setBlockNrEth(Long blockNrEth) {
        this.blockNrEth = blockNrEth;
        return this;
    }

    public Long getBlockNrBtc() {
        return blockNrBtc;
    }

    public Payin setBlockNrBtc(Long blockNrBtc) {
        this.blockNrBtc = blockNrBtc;
        return this;
    }

    public Payin copy() {
        Payin p = new Payin();
        p.blockNrBtc = blockNrBtc;
        p.blockNrEth = blockNrEth;
        p.creationDate = creationDate;
        p.time = time;
        p.wei = wei;
        p.satoshi = satoshi;
        p.walletAddress = walletAddress;
        p.id = id;
        return p;
    }
}
