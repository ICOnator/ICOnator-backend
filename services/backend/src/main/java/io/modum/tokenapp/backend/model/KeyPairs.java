package io.modum.tokenapp.backend.model;

import javax.persistence.*;

@Entity(name = "keypairs")
public class KeyPairs {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "public_btc", unique = true, nullable = false)
    private String publicBtc;

    @Column(name = "public_eth", unique = true, nullable = false)
    private String publicEth;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPublicBtc() {
        return publicBtc;
    }

    public void setPublicBtc(String publicBtc) {
        this.publicBtc = publicBtc;
    }

    public String getPublicEth() {
        return publicEth;
    }

    public void setPublicEth(String publicEth) {
        this.publicEth = publicEth;
    }
}
