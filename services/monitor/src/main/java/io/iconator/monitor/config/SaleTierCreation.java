package io.iconator.monitor.config;

import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * This class is only active on the development profile.
 * Mainly, this is used to easily run the monitor without having to
 * create the tiers with, e.g., the /tiers/create API call or directly
 * inserting to the DB. It's only a convenience class with default values.
 */
@Profile("dev")
@Configuration
public class SaleTierCreation {

    private final static Logger LOG = LoggerFactory.getLogger(SaleTierCreation.class);

    @Autowired
    private SaleTierService saleTierService;

    @PostConstruct
    public void createDevelopmentTiers() throws Exception {
        Instant now = Instant.now();
        Date startDateS1 = Date.from(now);
        Date endDateS1 = Date.from(now.plus(1, ChronoUnit.HOURS));
        Date startDateS2 = endDateS1;
        Date endDateS2 = Date.from(startDateS2.toInstant().plus(1, ChronoUnit.HOURS));
        SaleTier s1 = new SaleTier(
                1,
                "Development Tier 1",
                startDateS1,
                endDateS1,
                new BigDecimal("0.5"),
                new BigInteger("0"),
                new BigInteger("1000000000000000000000"),
                false,
                false
        );
        SaleTier s2 = new SaleTier(
                2,
                "Development Tier 2",
                startDateS2,
                endDateS2,
                new BigDecimal("0.0"),
                new BigInteger("0"),
                new BigInteger("0"),
                true,
                true
        );
        saleTierService.saveTransactionless(s1);
        saleTierService.saveTransactionless(s2);
        LOG.info("Development tiers successfully set-up.");
        LOG.info("Tier {}: startDate={} endDate={}", s1.getTierNo(), startDateS1, endDateS1);
        LOG.info("Tier {}: startDate={} endDate={}", s2.getTierNo(), startDateS2, endDateS2);
    }

}
