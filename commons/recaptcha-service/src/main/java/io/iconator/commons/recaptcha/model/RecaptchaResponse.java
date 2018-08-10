package io.iconator.commons.recaptcha.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecaptchaResponse {

    /**
     * {
     * "success": true|false,
     * "challenge_ts": timestamp,  // timestamp of the challenge load (ISO format yyyy-MM-dd'T'HH:mm:ssZZ)
     * "hostname": string,         // the hostname of the site where the reCAPTCHA was solved
     * "error-codes": [...]        // optional
     * }
     */

    @JsonProperty("success")
    private boolean success;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("challenge_ts")
    private Date timestamp;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("error-codes")
    private String[] errorCodes;

    public RecaptchaResponse() {
    }

    public RecaptchaResponse(boolean success, Date timestamp, String hostname) {
        this.success = success;
        this.timestamp = timestamp;
        this.hostname = hostname;
    }

    public RecaptchaResponse(boolean success, Date timestamp, String hostname, String[] errorCodes) {
        this.success = success;
        this.timestamp = timestamp;
        this.hostname = hostname;
        this.errorCodes = errorCodes;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String[] getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(String[] errorCodes) {
        this.errorCodes = errorCodes;
    }
}
