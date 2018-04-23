package io.iconator.core.service;

import io.iconator.commons.model.db.WhitelistEmail;
import io.iconator.commons.sql.dao.WhitelistEmailRepository;
import io.iconator.core.service.exception.WhitelistEmailNotSavedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class WhitelistEmailService {

    private final Logger LOG = LoggerFactory.getLogger(WhitelistEmailService.class);

    private final WhitelistEmailRepository whitelistEmailRepository;

    @Autowired
    public WhitelistEmailService(WhitelistEmailRepository whitelistEmailRepository) {
        assert whitelistEmailRepository != null;
        this.whitelistEmailRepository = whitelistEmailRepository;
    }

    public WhitelistEmail saveWhiteListEmail(String email) throws WhitelistEmailNotSavedException {
        LOG.info("Got new email for the whitelist " + email);
        Optional<WhitelistEmail> whitelistEmailFromDb = Optional.empty();
        try {
            whitelistEmailFromDb = Optional.ofNullable(this.whitelistEmailRepository.save(new WhitelistEmail(email, new Date())));
        } catch (DataIntegrityViolationException e) {
            LOG.info("Email {} was already subscribed.", email);
            whitelistEmailFromDb = this.whitelistEmailRepository.findByEmail(email);
        } catch (Exception e) {
            LOG.error("Could not save the email to the whitelist.", e);
        }
        return whitelistEmailFromDb.orElseThrow(() -> new WhitelistEmailNotSavedException());
    }

    public List<WhitelistEmail> getAllWhitelistEmails() {
        return new ArrayList<>(this.whitelistEmailRepository.findAll());
    }
}
