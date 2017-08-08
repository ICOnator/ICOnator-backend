package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.controller.exceptions.BaseException;
import io.modum.tokenapp.backend.controller.exceptions.ConfirmationTokenNotFoundException;
import io.modum.tokenapp.backend.controller.exceptions.UnexpectedException;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.service.FileQueueService;
import io.modum.tokenapp.backend.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@EnableWebMvc
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @Value("${modum.tokenapp.frontendUrl}")
    private String frontendUrl;

    @Value("${modum.tokenapp.frontendWalletPath}")
    private String frontendWalletUrlPath;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private FileQueueService fileQueueService;

    public RegisterController() {

    }

    @RequestMapping(value = "/register", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest,
                                      @Context HttpServletRequest httpServletRequest)
            throws BaseException {
        // Get IP address from request
        String ipAddress = httpServletRequest.getHeader("X-Real-IP");
        if (ipAddress == null)
            ipAddress = httpServletRequest.getRemoteAddr();

        URI uri = null;
        try {
            String emailConfirmationToken = null;
            Optional<Investor> oInvestor = investorRepository.findOptionalByEmail(registerRequest.getEmail());
            if (oInvestor.isPresent()) {
                emailConfirmationToken = oInvestor.get().getEmailConfirmationToken();
            } else {
                emailConfirmationToken = generateRandomUUID();
                oInvestor = Optional.of(createInvestor(registerRequest.getEmail(), emailConfirmationToken, ipAddress));
                Investor investor = oInvestor.get();
                investorRepository.save(investor);
                LOG.debug("Investor saved to the database: email="
                        + investor.getEmail() + "), emailConfirmationToken="
                        + investor.getEmailConfirmationToken());
            }
            // If the investor has a wallet, and all the payIn addresses set,
            // then, send the summary email.
            // Else, send the confirmation email with the confirmationEmailToken
            if (oInvestor.isPresent()
                    && oInvestor.get().getWalletAddress() != null
                    && oInvestor.get().getPayInBitcoinPublicKey() != null
                    && oInvestor.get().getPayInEtherPublicKey() != null){
                fileQueueService.addSummaryEmail(oInvestor.get());
                return ResponseEntity.ok().build();
            } else {
                uri = buildUri(emailConfirmationToken);
                fileQueueService.addConfirmationEmail(oInvestor.get(), uri);
                return ResponseEntity.created(uri).build();
            }

        } catch (Exception e) {
            LOG.error("Unexpected exception in RegisterController: {} {}", e.getMessage(), e.getCause());
            throw new UnexpectedException();
        }
    }

    @RequestMapping(value = "/register/{emailConfirmationToken}/validate", method = GET)
    public ResponseEntity<?> isConfirmationTokenValid(@Valid @Size(max = Constants.UUID_CHAR_MAX_SIZE) @PathVariable("emailConfirmationToken") String emailConfirmationToken,
                                          HttpServletResponse response)
            throws BaseException {
        Optional<Investor> oInvestor = Optional.empty();
        try {
            oInvestor = investorRepository.findOptionalByEmailConfirmationToken(emailConfirmationToken);
        } catch (Exception e) {
            throw new UnexpectedException();
        }
        if (!oInvestor.isPresent()) {
            throw new ConfirmationTokenNotFoundException();
        }
        return ResponseEntity.ok().build();
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private URI buildUri(String randomUUID) throws URISyntaxException {
        return new URI(frontendUrl + frontendWalletUrlPath + randomUUID);
    }

    private Investor createInvestor(String email, String randomUUID, String ipAddress) {
        return new Investor()
                .setCreationDate(new Date())
                .setEmail(email).setEmailConfirmationToken(randomUUID)
                .setIpAddress(ipAddress);
    }

}
