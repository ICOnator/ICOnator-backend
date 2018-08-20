package io.iconator.commons.cryptocompare;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.cryptocompare.config.CryptoCompareClientServiceTestConfig;
import io.iconator.commons.cryptocompare.config.CryptoCompareConfig;
import io.iconator.commons.cryptocompare.config.CryptoCompareConfigHolder;
import io.iconator.commons.cryptocompare.model.CryptoCompareConversionRateResponse;
import io.iconator.commons.cryptocompare.model.CryptoCompareConversionRates;
import io.iconator.commons.cryptocompare.model.CryptoCompareCurrency;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponse;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {
        CryptoCompareConfig.class,
        CryptoCompareConfigHolder.class,
        CryptoCompareClientService.class,
        CryptoCompareClientServiceTestConfig.class
})
@TestPropertySource("classpath:application-test.properties")
public class CryptoCompareClientServiceTest {

    private final static Logger LOG = LoggerFactory.getLogger(CryptoCompareClientServiceTest.class);

    @Autowired
    private CryptoCompareClientService cryptoCompareClientService;

    @Autowired
    private CryptoCompareConfigHolder cryptoCompareConfigHolder;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetHistorical() throws Exception {
        CryptoCompareConversionRateResponse rateResponseUSD = new CryptoCompareConversionRateResponse(CryptoCompareCurrency.USD, new BigDecimal(401.25));
        CryptoCompareConversionRateResponse rateResponseBTC = new CryptoCompareConversionRateResponse(CryptoCompareCurrency.BTC, new BigDecimal(0.0056));
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(Arrays.asList(rateResponseUSD, rateResponseBTC));
        List<CryptoCompareResponse> compareResponses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO responseDTO = new CryptoCompareResponseDTO(compareResponses);

        when(restTemplate.getForObject(any(), eq(CryptoCompareResponseDTO.class))).thenReturn(responseDTO);

        CryptoCompareResponseDTO actualResponseDTO = cryptoCompareClientService.getHistorical(
                CryptoCompareCurrency.ETH,
                Arrays.asList(CryptoCompareCurrency.USD, CryptoCompareCurrency.BTC),
                Instant.now().getEpochSecond()
        );

        assertEquals(actualResponseDTO, responseDTO);
    }

    @Test
    public void testGetPriceHistoricalURI() {
        URI actualUri = cryptoCompareClientService.getPriceHistoricalURI(
                CryptoCompareCurrency.ETH,
                Arrays.asList(CryptoCompareCurrency.CHF, CryptoCompareCurrency.USD),
                123456789
        );

        URI expectedUri = URI.create(
                String.format(
                        "%s/data/pricehistorical?fsym=%s&tsyms=%s&ts=%s&extraParams=%s",
                        cryptoCompareConfigHolder.getBaseUrl(),
                        "ETH",
                        "CHF,USD",
                        "123456789",
                        "ICOnator_client"
                )
        );

        assertEquals(expectedUri, actualUri);

    }

}