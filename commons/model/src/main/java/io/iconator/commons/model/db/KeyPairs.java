package io.iconator.commons.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name = "keypairs")
public class KeyPairs {

    @Id
    @SequenceGenerator(name = "id_seq", sequenceName = "keypairs_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "public_btc", unique = true, nullable = false)
    private String publicBtc;

    @Column(name = "public_eth", unique = true, nullable = false)
    private String publicEth;

    public KeyPairs() {
    }

    public KeyPairs(String publicBtc, String publicEth) {
        this.publicBtc = publicBtc;
        this.publicEth = publicEth;
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
}
