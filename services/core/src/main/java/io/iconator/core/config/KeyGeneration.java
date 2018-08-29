package io.iconator.core.config;

import io.iconator.commons.db.services.KeyPairsRepositoryService;
import io.iconator.commons.model.db.KeyPairs;
import io.iconator.core.service.CSVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@Profile("dev")
public class KeyGeneration {

    @Autowired
    private CSVService csvService;

    @Autowired
    private KeyPairsRepositoryService keyPairsRepositoryService;

    @Value(value = "classpath:publickeys-dev.csv")
    private Resource publicAddressFile;

    private static final Logger LOG = LoggerFactory.getLogger(KeyGeneration.class);

    @PostConstruct
    public void generateFreshKeys() throws Exception {
        List<KeyPairs> keyPairs = csvService.fromCSV(publicAddressFile.getInputStream());

        keyPairs.stream().forEach((keyPair) -> {
            keyPairsRepositoryService.addKeyPairsIfNotPresent(keyPair);
        });
    }
}
