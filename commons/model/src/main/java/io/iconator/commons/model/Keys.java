package io.iconator.commons.model;

public class Keys {

    private byte[] address;
    private String addressAsString;
    private byte[] publicKey;
    private String publicKeyAsHexString;
    private byte[] privateKey;
    private String privateKeyAsHexString;
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

    public byte[] getPublicKey() {
        return publicKey;
    }

    public Keys setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getPublicKeyAsHexString() {
        return publicKeyAsHexString;
    }

    public Keys setPublicKeyAsHexString(String publicKeyAsHexString) {
        this.publicKeyAsHexString = publicKeyAsHexString;
        return this;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public Keys setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getPrivateKeyAsHexString() {
        return privateKeyAsHexString;
    }

    public Keys setPrivateKeyAsHexString(String privateKeyAsHexString) {
        this.privateKeyAsHexString = privateKeyAsHexString;
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
