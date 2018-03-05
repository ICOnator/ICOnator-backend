package io.iconator.backend.dto;

import io.iconator.backend.utils.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RegisterRequest {

    @NotNull
    @Size(max = Constants.EMAIL_CHAR_MAX_SIZE)
    private String email;

    public String getEmail() {
        return email;
    }

    public RegisterRequest setEmail(String email) {
        this.email = email;
        return this;
    }

}
