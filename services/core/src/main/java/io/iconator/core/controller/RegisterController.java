package io.iconator.core.controller;

import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.recaptcha.RecaptchaClientService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.core.controller.exceptions.BaseException;
import io.iconator.core.controller.exceptions.ConfirmationTokenNotFoundException;
import io.iconator.core.controller.exceptions.RecaptchaInvalidException;
import io.iconator.core.controller.exceptions.UnexpectedException;
import io.iconator.core.dto.AddressResponse;
import io.iconator.core.dto.RegisterRequest;
import io.iconator.core.utils.Constants;
import io.iconator.core.utils.IPAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @Value("${io.iconator.services.core.frontendUrl}")
    private String frontendUrl;

    @Value("${io.iconator.services.core.frontendWalletPath}")
    private String frontendWalletUrlPath;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private ICOnatorMessageService messageService;

    @Autowired
    private RecaptchaClientService recaptchaClientService;

    public RegisterController() {

    }

    @RequestMapping(value = "/register", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> register(@RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponseToken,
                                      @Valid @RequestBody RegisterRequest registerRequest,
                                      @Context HttpServletRequest requestContext)
            throws BaseException {
        // Get IP address from request
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        // Verify captcha
        boolean recaptchaVerified = recaptchaClientService.verify(ipAddress, recaptchaResponseToken);
        if (!recaptchaVerified) {
            throw new RecaptchaInvalidException();
        }

        LOG.info("/register called from {} with email: {}", ipAddress, registerRequest.getEmail());

        try {
            String emailConfirmationToken = null;
            Optional<Investor> oInvestor = investorRepository.findOptionalByEmail(registerRequest.getEmail());
            if (oInvestor.isPresent()) {
                emailConfirmationToken = oInvestor.get().getEmailConfirmationToken();
            } else {
                emailConfirmationToken = generateRandomUUID();
                oInvestor = Optional.of(createInvestor(registerRequest.getEmail(), emailConfirmationToken, ipAddress));
                Investor investor = oInvestor.get();
                investorRepository.saveAndFlush(investor);
                LOG.debug("Investor saved to the database: email="
                        + investor.getEmail() + "), emailConfirmationToken="
                        + investor.getEmailConfirmationToken());
            }
            // If the investor has a wallet, and all the payIn addresses set,
            // then, send the summary email.
            // Else, send the confirmation email with the confirmationEmailToken
            if (oInvestor.isPresent()
                    && oInvestor.get().getWalletAddress() != null
                    && oInvestor.get().getPayInBitcoinAddress() != null
                    && oInvestor.get().getPayInEtherAddress() != null) {
                SummaryEmailMessage summaryEmailMessage = new SummaryEmailMessage(build(oInvestor.get()));
                messageService.send(summaryEmailMessage);
                return ResponseEntity.ok().build();
            } else {
                URI emailLinkUri = new URI(frontendUrl + frontendWalletUrlPath + emailConfirmationToken);
                ConfirmationEmailMessage confirmationEmailMessage = new ConfirmationEmailMessage(build(oInvestor.get()), emailLinkUri.toASCIIString());
                messageService.send(confirmationEmailMessage);
                return ResponseEntity.created(new URI(frontendUrl)).build();
            }

        } catch (Exception e) {
            LOG.error("Unexpected exception in RegisterController: {} {}", e.getMessage(), e.getCause());
            throw new UnexpectedException();
        }
    }

    @RequestMapping(value = "/register/{emailConfirmationToken}/validate", method = GET)
    public ResponseEntity<?> isConfirmationTokenValid(@Valid @Size(max = Constants.UUID_CHAR_MAX_SIZE) @PathVariable("emailConfirmationToken") String emailConfirmationToken,
                                                      @Context HttpServletRequest requestContext)
            throws BaseException {
        // Get IP address from request
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/validate called from {} with token {}", ipAddress, emailConfirmationToken);

        Optional<Investor> oInvestor = Optional.empty();
        try {
            oInvestor = investorRepository.findOptionalByEmailConfirmationToken(emailConfirmationToken);
        } catch (Exception e) {
            throw new UnexpectedException();
        }
        if (!oInvestor.isPresent()) {
            throw new ConfirmationTokenNotFoundException();
        }
        if (oInvestor.get().getWalletAddress() == null) {
            return ResponseEntity.ok().build();
        } else {
            AddressResponse addressResponse = new AddressResponse()
                    .setBtc(oInvestor.get().getPayInBitcoinAddress())
                    .setEther(oInvestor.get().getPayInEtherAddress());
            return new ResponseEntity<>(addressResponse, HttpStatus.OK);
        }
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private Investor createInvestor(String email, String randomUUID, String ipAddress) {
        return new Investor(new Date(), email, randomUUID, ipAddress);
    }

}
