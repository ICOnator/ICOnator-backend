package io.modum.tokenapp.backend.bean;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class EthereumKeyGeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumKeyGeneratorTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGenerateAndValidateAddress() throws IOException {
        EthereumKeyGenerator ethereumKeyGenerator = new EthereumKeyGenerator();
        Keys keys = ethereumKeyGenerator.getKeys();
        String jsonKeys = objectMapper.writer().writeValueAsString(keys);
        LOG.info(jsonKeys);
        assertTrue(ethereumKeyGenerator.isValidAddress(keys.getAddress()));
        assertTrue(ethereumKeyGenerator.isValidAddress(keys.getAddressAsString()));
    }
}
