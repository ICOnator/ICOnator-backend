package io.modum.tokenapp.backend.bean;

import org.spongycastle.util.encoders.Hex;

public class Keys {

    private byte[] address;
    private byte[] privateKey;

    public byte[] getAddress() {
        return address;
    }

    public String getAddressBase16() {
        return Hex.toHexString(getAddress());
    }

    public Keys setAddress(byte[] address) {
        this.address = address;
        return this;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public String getPrivateKeyBase16() {
        return Hex.toHexString(getPrivateKey());
    }

    public Keys setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
        return this;
    }

}
