package io.iconator.core.controller;

import io.iconator.commons.model.db.WhitelistEmail;
import io.iconator.core.service.WhitelistEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class WhitelistEmailController {

    @Autowired
    private WhitelistEmailService whitelistEmailService;

    @RequestMapping(value = "/whitelist", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public WhitelistEmail insertWhiteListEmail(@RequestBody WhitelistEmail whitelistEmail) {
        return this.whitelistEmailService.insertWhiteListEmail(whitelistEmail);
    }

    @RequestMapping(value = "/emails", method = GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<WhitelistEmail> getEmails() {
        return this.whitelistEmailService.getAllMails();
    }

}
