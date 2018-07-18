package io.iconator.kyc.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Identification {

    private UUID kycUuid;
    private String result;
    private Date identificationTime;

    public Identification() {}

    public Identification(UUID kycUuid, String result, Date identificationTime) {
        this.kycUuid = kycUuid;
        this.result = result;
        this.identificationTime = identificationTime;
    }

    public UUID getKycUuid() {
        return kycUuid;
    }

    public String getResult() {
        return result;
    }

    public Date getIdentificationTime() {
        return identificationTime;
    }

    public void setKycUuid(UUID kycUuid) {
        this.kycUuid = kycUuid;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setIdentificationTime(Date identificationTime) {
        this.identificationTime = identificationTime;
    }
}

