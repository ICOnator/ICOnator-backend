package io.iconator.kyc.dto;

import java.util.List;
import java.util.UUID;

public class FetchAllResponseDTO {

    private List<UUID> kycCompletedList;

    private List<UUID> errorList;

    public List<UUID> getKycCompletedList() {
        return kycCompletedList;
    }

    public List<UUID> getErrorList() {
        return errorList;
    }

    public FetchAllResponseDTO setKycCompletedList(List<UUID> kycCompletedList) {
        this.kycCompletedList = kycCompletedList;
        return this;
    }

    public FetchAllResponseDTO setErrorList(List<UUID> errorList) {
        this.errorList = errorList;
        return this;
    }

}
