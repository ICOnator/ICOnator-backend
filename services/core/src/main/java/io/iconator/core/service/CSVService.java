package io.iconator.core.service;

import io.iconator.commons.model.db.KeyPairs;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class CSVService {

    private final Logger LOG = LoggerFactory.getLogger(CSVService.class);

    public List<KeyPairs> fromCSV(InputStream inputStream) throws Exception {
        Reader reader = new InputStreamReader(inputStream);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
        Iterable<CSVRecord> iterable = () -> records.iterator();
        Stream<CSVRecord> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.map((entry) -> {
            String ethAddress = entry.get(0);
            String btcAddress = entry.get(1);
            LOG.info("Reading: ETH={} BTC={}", ethAddress, btcAddress);
            return new KeyPairs(btcAddress, ethAddress, true);
        }).collect(Collectors.toList());
    }

}
