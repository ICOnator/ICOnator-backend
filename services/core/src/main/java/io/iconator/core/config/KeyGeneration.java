package io.iconator.core.config;

import io.iconator.commons.db.services.KeyPairsRepositoryService;
import io.iconator.commons.model.db.KeyPairs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Configuration
@Profile("dev")
public class KeyGeneration {


    @Autowired
    private KeyPairsRepositoryService keyPairsRepositoryService;

    @Value(value = "classpath:publickeys-dev.csv")
    private Resource publicAddressFile;

    private static final Logger LOG = LoggerFactory.getLogger(KeyGeneration.class);

    @PostConstruct
    public void generateFreshKeys() {
        String line;
        try (
                InputStream fis = publicAddressFile.getInputStream();
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                String[] strArr=line.split(",");
                if(strArr[0].startsWith("Ethereum")) {
                    continue;
                }
                KeyPairs keyPairs = new KeyPairs(strArr[1], strArr[0], Boolean.TRUE);
                keyPairsRepositoryService.addKeyPairsIfNotPresent(keyPairs);
                LOG.debug("added bitcoin and ethereum keys: [{}, {}]",
                        keyPairs.getPublicBtc(), keyPairs.getPublicEth());
            }

        } catch (Exception e) {
            LOG.error("cannot read file", e);
        }
    }
}
