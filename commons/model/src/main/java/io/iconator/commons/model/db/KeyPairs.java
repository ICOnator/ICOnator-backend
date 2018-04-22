package io.iconator.commons.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity(name = "keypairs")
public class KeyPairs {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "public_btc", unique = true, nullable = false)
    private String publicBtc;

    @Column(name = "public_eth", unique = true, nullable = false)
    private String publicEth;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    public KeyPairs() {
    }

    public KeyPairs(String publicBtc, String publicEth) {
        this.publicBtc = publicBtc;
        this.publicEth = publicEth;
    }

    public KeyPairs(String publicBtc, String publicEth, Boolean available) {
        this.publicBtc = publicBtc;
        this.publicEth = publicEth;
        this.available = available;
    }

    public long getId() {
        return id;
    }

    public String getPublicBtc() {
        return publicBtc;
    }

    public String getPublicEth() {
        return publicEth;
    }

    public Boolean getAvailable() {
        return available;
    }

    public KeyPairs setAvailable(Boolean available) {
        this.available = available;
        return this;
    }

}
