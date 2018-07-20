package io.iconator.commons.amqp.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.model.db.Investor;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestorMessageDTO {

    private Date creationDate;

    private String email;

    private String emailConfirmationToken;

    private String walletAddress;

    private String payInEtherAddress;

    private String payInBitcoinAddress;

    private String refundEtherAddress;

    private String refundBitcoinAddress;

    private String ipAddress;

    public InvestorMessageDTO() {
    }

    public InvestorMessageDTO(Date creationDate, String email, String emailConfirmationToken, String walletAddress, String payInEtherAddress, String payInBitcoinAddress, String refundEtherAddress, String refundBitcoinAddress, String ipAddress) {
        this.creationDate = creationDate;
        this.email = email;
        this.emailConfirmationToken = emailConfirmationToken;
        this.walletAddress = walletAddress;
        this.payInEtherAddress = payInEtherAddress;
        this.payInBitcoinAddress = payInBitcoinAddress;
        this.refundEtherAddress = refundEtherAddress;
        this.refundBitcoinAddress = refundBitcoinAddress;
        this.ipAddress = ipAddress;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailConfirmationToken() {
        return emailConfirmationToken;
    }

    public void setEmailConfirmationToken(String emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getPayInEtherAddress() {
        return payInEtherAddress;
    }

    public void setPayInEtherAddress(String payInEtherAddress) {
        this.payInEtherAddress = payInEtherAddress;
    }

    public String getPayInBitcoinAddress() {
        return payInBitcoinAddress;
    }

    public void setPayInBitcoinAddress(String payInBitcoinAddress) {
        this.payInBitcoinAddress = payInBitcoinAddress;
    }

    public String getRefundEtherAddress() {
        return refundEtherAddress;
    }

    public void setRefundEtherAddress(String refundEtherAddress) {
        this.refundEtherAddress = refundEtherAddress;
    }

    public String getRefundBitcoinAddress() {
        return refundBitcoinAddress;
    }

    public void setRefundBitcoinAddress(String refundBitcoinAddress) {
        this.refundBitcoinAddress = refundBitcoinAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Investor toInvestor() {
        return new Investor(
                getCreationDate(),
                getEmail(),
                getEmailConfirmationToken(),
                getWalletAddress(),
                getPayInEtherAddress(),
                getPayInBitcoinAddress(),
                getRefundEtherAddress(),
                getRefundBitcoinAddress(),
                getIpAddress()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InvestorMessageDTO investor = (InvestorMessageDTO) o;

        // TODO: is it enough?
        return Arrays.equals(email == null ? null : email.getBytes(),
                investor.getEmail() == null ? null : investor.getEmail().getBytes());
    }

    @Override
    public int hashCode() {
        // TODO: is it enough?
        return Objects.hash(Arrays.hashCode(email.toCharArray()));
    }

}
