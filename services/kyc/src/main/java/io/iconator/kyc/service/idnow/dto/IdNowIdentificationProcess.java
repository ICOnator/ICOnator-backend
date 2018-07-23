package io.iconator.kyc.service.idnow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.kyc.dto.Identification;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdNowIdentificationProcess implements Identification {

    /**
     * "result": "SUCCESS",
     * "agentname": "HMUELLER",
     * "identificationtime": "2014-06-02T05:03:54Z",
     * "type": "WEB",
     * "transactionnumber": "c02f9eea-bdef-4723-8ec3-eb254c2039f7",
     * "companyid": "ihrebank",
     * "id": "4c000024-bde8-4dd2-952f-f36ff91b0eb0"
     */

    @JsonProperty("result")
    private String result;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("identificationtime")
    private Date identificationTime;

    @JsonProperty("transactionnumber")
    private String transactionNumber;

    @JsonProperty("id")
    private String id;

    public IdNowIdentificationProcess() {
    }

    public IdNowIdentificationProcess(String result, Date identificationTime, String transactionNumber) {
        this.result = result;
        this.identificationTime = identificationTime;
        this.transactionNumber = transactionNumber;
    }

    public String getResult() {
        return result;
    }

    public Date getIdentificationTime() {
        return identificationTime;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getId() {
        return id;
    }
}
