package io.iconator.core.controller;

import io.iconator.commons.model.db.WhitelistEmail;
import io.iconator.core.controller.exceptions.BaseException;
import io.iconator.core.controller.exceptions.UnexpectedException;
import io.iconator.core.dto.WhitelistEmailRequest;
import io.iconator.core.dto.WhitelistEmailResponse;
import io.iconator.core.service.WhitelistEmailService;
import io.iconator.core.service.exceptions.WhitelistEmailNotSavedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class WhitelistEmailController {

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistEmailController.class);

    @Autowired
    private WhitelistEmailService whitelistEmailService;

    @RequestMapping(value = "/whitelist/subscribe", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> saveWhiteListEmail(@Valid @RequestBody WhitelistEmailRequest whitelistEmailRequest) throws BaseException {
        try {
            WhitelistEmail whitelistEmail = this.whitelistEmailService.saveWhiteListEmail(whitelistEmailRequest.getEmail());
            return new ResponseEntity<>(new WhitelistEmailResponse(whitelistEmail.getEmail(), whitelistEmail.getSubscriptionDate()),
                    HttpStatus.OK);
        } catch (WhitelistEmailNotSavedException e) {
            throw new UnexpectedException();
        }
    }

    @RequestMapping(value = "/whitelist/emails", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<WhitelistEmail> getEmails() throws BaseException {
        return this.whitelistEmailService.getAllWhitelistEmails();
    }

}
