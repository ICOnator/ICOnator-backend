package io.iconator.commons.recaptcha;

import io.iconator.commons.recaptcha.config.RecaptchaConfigHolder;
import io.iconator.commons.recaptcha.exceptions.*;
import io.iconator.commons.recaptcha.model.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class RecaptchaClientService {

    public static final String MISSING_INPUT_SECRET = "missing-input-secret";
    public static final String INVALID_INPUT_SECRET = "invalid-input-secret";
    public static final String MISSING_INPUT_RESPONSE = "missing-input-response";
    public static final String INVALID_INPUT_RESPONSE = "invalid-input-response";
    public static final String RECAPTCHA_BASE_URL = "https://www.google.com";

    private RecaptchaConfigHolder recaptchaConfigHolder;
    private RestTemplate restTemplate;

    @Autowired
    public RecaptchaClientService(@Qualifier("restTemplateRecaptcha") RestTemplate restTemplate,
                                  RecaptchaConfigHolder recaptchaConfigHolder) {
        this.restTemplate = restTemplate;
        this.recaptchaConfigHolder = recaptchaConfigHolder;
    }

    public boolean verify(String ipAddress, String recaptchaResponse) throws RecaptchaException {

        if (!recaptchaConfigHolder.isEnabled()) {
            return true;
        }

        if (recaptchaResponse == null) {
            return false;
        }

        RequestEntity requestEntity = RequestEntity
                .post(getSiteVerifyURI(ipAddress, recaptchaResponse))
                .accept(MediaType.APPLICATION_JSON)
                .build();

        ResponseEntity<RecaptchaResponse> responseEntity = restTemplate.exchange(requestEntity, RecaptchaResponse.class);

        if (responseEntity != null && responseEntity.getBody() != null) {
            if (responseEntity.getBody().isSuccess()) {
                return true;
            } else {
                String[] errorCodes = responseEntity.getBody().getErrorCodes();
                for (String errorCode : errorCodes) {
                    throw getException(errorCode);
                }
            }
        }

        // unexpected exception
        throw new RecaptchaException("Error in the recaptcha verify client.");
    }

    private URI getSiteVerifyURI(String ipAddress, String recaptchaResponse) {
        return UriComponentsBuilder.fromUriString(RECAPTCHA_BASE_URL)
                .path("/recaptcha")
                .path("/api")
                .path("/siteverify")
                .queryParam("secret", this.recaptchaConfigHolder.getSecretKey())
                .queryParam("response", recaptchaResponse)
                .queryParam("remoteip", ipAddress)
                .build()
                .toUri();
    }

    private RecaptchaException getException(String errorCode) {
        switch (errorCode) {
            case MISSING_INPUT_SECRET:
                return new MissingInputSecretException("The secret key was not provided.");
            case INVALID_INPUT_SECRET:
                return new InvalidInputSecretException("The secret key provided is invalid.");
            case MISSING_INPUT_RESPONSE:
                return new MissingInputResponseException("The recaptcha response was not provided.");
            case INVALID_INPUT_RESPONSE:
                return new InvalidInputResponseException("The recaptcha response provided is invalid.");
            default:
                return new RecaptchaException("Error in the Recaptcha verify call.");
        }
    }

}
