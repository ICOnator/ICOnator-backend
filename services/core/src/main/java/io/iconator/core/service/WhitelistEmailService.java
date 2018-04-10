package io.iconator.core.service;

import io.iconator.commons.model.db.WhitelistEmail;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.commons.sql.dao.WhitelistEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class WhitelistEmailService {


    private final Logger log = LoggerFactory.getLogger(WhitelistEmailService.class);
    private final WhitelistEmailRepository whitelistEmailRepository;

    @Autowired
    public WhitelistEmailService(WhitelistEmailRepository whitelistEmailRepository) {
        assert whitelistEmailRepository != null;
        this.whitelistEmailRepository = whitelistEmailRepository;
    }


    public WhitelistEmail insertWhiteListEmail(WhitelistEmail whitelistEmail) {
        log.info("Got new email for the whitelist " + whitelistEmail.toString());
        Optional<WhitelistEmail> whitelistEmailOptional = this.whitelistEmailRepository.findByEmail(whitelistEmail.getEmail());
        if (whitelistEmailOptional.isPresent()) {
            whitelistEmail = whitelistEmailOptional.get();
        }
        else {
            String email = whitelistEmail.getEmail();
            whitelistEmail = new WhitelistEmail(email, new Date());
            this.whitelistEmailRepository.save(whitelistEmail);
        }
        return whitelistEmail;
    }

    public List<WhitelistEmail> getAllMails() {
        return new ArrayList<>(this.whitelistEmailRepository.findAll());
    }
}
