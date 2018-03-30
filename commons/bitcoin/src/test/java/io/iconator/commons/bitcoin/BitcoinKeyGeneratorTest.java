package io.iconator.commons.bitcoin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.bitcoin.config.BitcoinConfig;
import io.iconator.commons.model.Keys;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BitcoinConfig.class})
@TestPropertySource("classpath:application-test.properties")
public class BitcoinKeyGeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinKeyGeneratorTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BitcoinConfig bitcoinConfig;

    @Test
    public void testGenerateAndValidateAddress() throws IOException {
        BitcoinKeyGenerator bitcoinKeyGenerator = new BitcoinKeyGenerator(bitcoinConfig);
        Keys keys = bitcoinKeyGenerator.getKeys();
        String jsonKeys = objectMapper.writer().writeValueAsString(keys);
        LOG.info(jsonKeys);
        assertTrue(bitcoinKeyGenerator.isValidAddress(keys.getAddressAsString()));
    }

}
