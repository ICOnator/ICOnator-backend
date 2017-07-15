package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.controller.exceptions.RegisterConfirmationException;
import io.modum.tokenapp.backend.controller.exceptions.RegisterException;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import io.modum.tokenapp.backend.model.Investor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @Value("${modum.tokenapp.frontendUrl}")
    private String frontendUrl;

    @Value("${modum.tokenapp.frontendWalletPath}")
    private String frontendWalletPath;

    @Autowired
    private InvestorRepository investorRepository;

    public RegisterController() {

    }

    @RequestMapping(value = "/register", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest)
            throws RegisterException {
        URI uri = null;
        String randomUUID = generateRandomUUID();
        try {
            uri = buildUri(randomUUID);
            Investor investor = createInvestor(registerRequest.getEmail(), randomUUID);
            investorRepository.save(investor);
            LOG.debug("Investor saved to the database: email="
                    + investor.getEmail() + "), emailConfirmationToken="
                    + investor.getEmailConfirmationToken());
        } catch (Exception e) {
            throw new RegisterException();
        }
        return ResponseEntity.created(uri).build();
    }

    @RequestMapping(value = "/register/{emailConfirmationToken}", method = GET)
    public ResponseEntity<?> confirmation(@PathVariable("emailConfirmationToken") String emailConfirmationToken,
                                          HttpServletResponse response)
            throws RegisterConfirmationException {
        try {
            Optional<Investor> oInvestor = investorRepository.findOptionalByEmailConfirmationToken(emailConfirmationToken);
            if (!oInvestor.isPresent()) {
                throw new RegisterConfirmationException();
            } else {
                Investor investor = oInvestor.get();
                investor.setEmailConfirmed(true);
                investorRepository.save(investor);
                LOG.debug("Investor confirmed email address: email=" + investor.getEmail()
                        + ", emailConfirmationToken=" + investor.getEmailConfirmationToken());
            }
        } catch (Exception e) {
            throw e;
        }
        return ResponseEntity.ok().build();
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private URI buildUri(String randomUUID) throws URISyntaxException {
        return new URI(frontendUrl + frontendWalletPath + randomUUID);
    }

    private Investor createInvestor(String email, String randomUUID) {
        return new Investor().setCreationDate(new Date()).setEmail(email).setEmailConfirmationToken(randomUUID);
    }

}
