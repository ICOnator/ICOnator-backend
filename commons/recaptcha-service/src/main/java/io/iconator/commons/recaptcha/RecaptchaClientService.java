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
import java.util.Arrays;
import java.util.List;

/**
 * Provides verfication for recaptcha challenge responses.
 */
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

    /**
     * Verfies the given recaptcha response token against Googles recaptcha API.
     * @param ipAddress The IP address of the user that responded to the recaptcha challenge.
     * @param recaptchaResponse The recaptcha response token.
     * @return true if the verification was successful. False if not.
     * @throws RecaptchaException if the request to the recaptcha API failed because of other reasons.
     */
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

        if (responseEntity.getBody() != null) {
            if (responseEntity.getBody().isSuccess()) {
                return true;
            } else {
                List<String> errorCodes = Arrays.asList(responseEntity.getBody().getErrorCodes());
                if (errorCodes.contains(MISSING_INPUT_RESPONSE) || errorCodes.contains(INVALID_INPUT_RESPONSE)) {
                    return false;
                } else {
                    throw getException(errorCodes);
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

    private RecaptchaException getException(List<String> errorCodes) {
        if (errorCodes.contains(MISSING_INPUT_SECRET)) {
            return new MissingInputSecretException("The secret key was not provided.");
        } else if (errorCodes.contains(INVALID_INPUT_SECRET)) {
            return new InvalidInputSecretException("The secret key provided is invalid.");
        } else{
            return new RecaptchaException("Error in the Recaptcha verify call.");
        }
    }
}
