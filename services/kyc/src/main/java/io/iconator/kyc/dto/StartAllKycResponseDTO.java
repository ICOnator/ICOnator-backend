package io.iconator.kyc.dto;

import java.util.List;

public class StartAllKycResponseDTO {

    private List<Long> kycStartedList;

    private List<Long> errorList;

    public List<Long> getKycStartedList() {
        return kycStartedList;
    }

    public List<Long> getErrorList() {
        return errorList;
    }

    public StartAllKycResponseDTO setKycStartedList(List<Long> kycStartedList) {
        this.kycStartedList = kycStartedList;
        return this;
    }

    public StartAllKycResponseDTO setErrorList(List<Long> errorList) {
        this.errorList = errorList;
        return this;
    }

}
