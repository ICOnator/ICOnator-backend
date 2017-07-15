package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.controller.exceptions.RegisterConfirmationException;
import io.modum.tokenapp.backend.controller.exceptions.RegisterException;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @Value("${modum.tokenapp.frontendUrl}")
    private String frontendUrl;

    public RegisterController() {

    }

    @RequestMapping(value = "/register", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response)
            throws RegisterException {
        URI uri = null;
        try {
            uri = new URI(frontendUrl + "/frontend/wallet/" + UUID.randomUUID().toString());
            // TODO: here goes the logic to persist the investor

            // TODO: if persisted, then send the email with the generated uri

        } catch (Exception e) {
            throw new RegisterException();
        }
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value = "/register/{emailConfirmationToken}", method = GET)
    public ResponseEntity<?> confirmation(@PathVariable("emailConfirmationToken") String emailConfirmationToken, HttpServletResponse response)
            throws RegisterConfirmationException {

        // TODO: search which investor has such emailConfirmationToken
        // TODO: if found the investor, set the Investor.emailConfirmed to 'true'
        // TODO: else, return an 'error'

        return ResponseEntity.ok().build();
    }

}
