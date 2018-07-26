package io.iconator.kyc.dto;

public class KycStartRequestDTO {

    private String kycLink;

    public KycStartRequestDTO() {}

    public KycStartRequestDTO(String kycLink) {
        this.kycLink = kycLink;
    }

    public String getKycLink() {
        return kycLink;
    }

    public KycStartRequestDTO setKycLink(String kycLink) {
        this.kycLink = kycLink;
        return this;
    }
}
