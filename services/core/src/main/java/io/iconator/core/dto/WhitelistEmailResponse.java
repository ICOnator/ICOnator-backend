package io.iconator.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.core.utils.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhitelistEmailResponse {

    @NotNull
    @Size(max = Constants.EMAIL_CHAR_MAX_SIZE)
    @JsonProperty("email")
    private String email;

    @JsonProperty("subscriptionDate")
    private Date subscriptionDate;

    public WhitelistEmailResponse() {
    }

    public WhitelistEmailResponse(@NotNull @Size(max = Constants.EMAIL_CHAR_MAX_SIZE) String email, Date subscriptionDate) {
        this.email = email;
        this.subscriptionDate = subscriptionDate;
    }

    public String getEmail() {
        return email;
    }

    public Date getSubscriptionDate() {
        return subscriptionDate;
    }
}
