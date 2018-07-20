package io.iconator.core.service;

import io.iconator.commons.db.services.KeyPairsRepositoryService;
import io.iconator.commons.model.db.KeyPairs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class ApplicationStartup
        implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationStartup.class);

    @Value(value = "classpath:publickeys-dev.csv")
    private Resource publicKeysFile;

    @Autowired
    private KeyPairsRepositoryService keyPairsRepositoryService;

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        String line;
        try (
                InputStream fis = publicKeysFile.getInputStream();
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
