package io.modum.tokenapp.backend.bean;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class BitcoinKeyGeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinKeyGeneratorTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGenerateAndValidateAddress() throws IOException {
        BitcoinKeyGenerator bitcoinKeyGenerator = new BitcoinKeyGenerator();
        Keys keys = bitcoinKeyGenerator.getKeys();
        String jsonKeys = objectMapper.writer().writeValueAsString(keys);
        LOG.info(jsonKeys);
        assertTrue(bitcoinKeyGenerator.isValidAddress(keys.getAddressAsString()));
    }

}
