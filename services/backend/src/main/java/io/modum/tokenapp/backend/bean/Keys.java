package io.modum.tokenapp.backend.bean;

public class Keys {

    private byte[] address;
    private String addressAsString;
    private byte[] privateKey;
    private String addressAsStringWithPrivate;

    public byte[] getAddress() {
        return address;
    }

    public Keys setAddress(byte[] address) {
        this.address = address;
        return this;
    }

    public String getAddressAsString() {
        return addressAsString;
    }

    public Keys setAddressAsString(String addressAsString) {
        this.addressAsString = addressAsString;
        return this;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public Keys setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getAddressAsStringWithPrivate() {
        return addressAsStringWithPrivate;
    }

    public Keys setAddressAsStringWithPrivate(String addressAsStringWithPrivate) {
        this.addressAsStringWithPrivate = addressAsStringWithPrivate;
        return this;
    }
}
