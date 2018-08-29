package io.iconator.core.controller;

import io.iconator.commons.db.services.KeyPairsRepositoryService;
import io.iconator.commons.model.db.KeyPairs;
import io.iconator.core.controller.exceptions.CSVImportException;
import io.iconator.core.dto.KeyPairsImportResponse;
import io.iconator.core.service.CSVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/keypairs")
public class KeyPairsController {

    private static final Logger LOG = LoggerFactory.getLogger(KeyPairsController.class);

    @Autowired
    private CSVService csvService;

    @Autowired
    private KeyPairsRepositoryService keyPairsRepositoryService;

    @RequestMapping(value = "/import", method = POST, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public KeyPairsImportResponse importKeyPairs(InputStream inputStream) throws CSVImportException {
        try {
            List<KeyPairs> keyPairs = csvService.fromCSV(inputStream);

            List<Boolean> result = keyPairs.stream().map((keyPair) -> {
                return keyPairsRepositoryService.addKeyPairsIfNotPresent(keyPair);
            }).collect(Collectors.toList());

            return new KeyPairsImportResponse(
                    result.stream().filter((entry) -> entry == true).count(),
                    result.stream().filter((entry) -> entry == false).count()
            );
        } catch (Exception e) {
            throw new CSVImportException();
        }
    }

}
