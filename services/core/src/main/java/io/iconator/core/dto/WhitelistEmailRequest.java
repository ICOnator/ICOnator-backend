package io.iconator.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.core.utils.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhitelistEmailRequest {

    @NotNull
    @Size(max = Constants.EMAIL_CHAR_MAX_SIZE)
    @JsonProperty("email")
    private String email;

    public WhitelistEmailRequest() {
    }

    public WhitelistEmailRequest(@NotNull @Size(max = Constants.EMAIL_CHAR_MAX_SIZE) String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
