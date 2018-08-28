package io.iconator.commons.cryptocompare;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.cryptocompare.config.CryptoCompareClientServiceTestConfig;
import io.iconator.commons.cryptocompare.model.CryptoCompareConversionRateResponse;
import io.iconator.commons.cryptocompare.model.CryptoCompareConversionRates;
import io.iconator.commons.cryptocompare.model.CryptoCompareCurrency;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponse;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        CryptoCompareClientServiceTestConfig.class
})
public class CryptoCompareModelTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUnmarshall() throws IOException {

        String json = "{\"ETH\":{\"BTC\":1.2345,\"USD\":5.12345}}";

        CryptoCompareResponseDTO result = objectMapper.reader().forType(CryptoCompareResponseDTO.class).readValue(json);
        assertNotNull(result.getResponse());
        assertEquals(1, result.getResponse().size());
        assertEquals("ETH", result.getResponse().stream().findFirst().get().getCryptoCompareCurrency().getName());
        assertEquals(1, result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().stream().filter((e) -> e.getCryptoCompareCurrency() == CryptoCompareCurrency.BTC).count());
        assertEquals(new BigDecimal("1.2345"), result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().stream().filter((e) -> e.getCryptoCompareCurrency() == CryptoCompareCurrency.BTC).findFirst().get().getRateValue());
        assertEquals(1, result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().stream().filter((e) -> e.getCryptoCompareCurrency() == CryptoCompareCurrency.USD).count());
        assertEquals(new BigDecimal("5.12345"), result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().stream().filter((e) -> e.getCryptoCompareCurrency() == CryptoCompareCurrency.USD).findFirst().get().getRateValue());
    }

    @Test
    public void testUnmarshall_NotSupportedCurrency_From() throws IOException {

        String json = "{\"BLAHHH\":{\"BTC\":1.2345,\"USD\":5.12345}}";

        CryptoCompareResponseDTO result = objectMapper.reader().forType(CryptoCompareResponseDTO.class).readValue(json);
        assertNotNull(result.getResponse());
        assertEquals(0, result.getResponse().size());
    }

    @Test
    public void testUnmarshall_NotSupportedCurrency_To() throws IOException {

        String json = "{\"ETH\":{\"BLAHHH\":1.2345,\"USD\":5.12345}}";

        CryptoCompareResponseDTO result = objectMapper.reader().forType(CryptoCompareResponseDTO.class).readValue(json);
        assertNotNull(result.getResponse());
        assertEquals(1, result.getResponse().size());
        assertEquals("ETH", result.getResponse().stream().findFirst().get().getCryptoCompareCurrency().getName());
        assertEquals(1, result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().size());
        assertEquals(1, result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().stream().filter((e) -> e.getCryptoCompareCurrency() == CryptoCompareCurrency.USD).count());
        assertEquals(new BigDecimal("5.12345"), result.getResponse().stream().findFirst().get().getRates().getConversionRateResponses().stream().filter((e) -> e.getCryptoCompareCurrency() == CryptoCompareCurrency.USD).findFirst().get().getRateValue());
    }

    @Test
    public void testMarshall() throws JsonProcessingException {
        List<CryptoCompareConversionRateResponse> rateResponses = Arrays.asList(
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.BTC, new BigDecimal("1.2345")),
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.USD, new BigDecimal("5.12345"))
        );
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(rateResponses);
        List<CryptoCompareResponse> responses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO dto = new CryptoCompareResponseDTO(responses);

        String result = objectMapper.writer().writeValueAsString(dto);

        assertEquals("{\"ETH\":{\"BTC\":1.2345,\"USD\":5.12345}}", result);

    }

    @Test
    public void testGetRateValue() {
        List<CryptoCompareConversionRateResponse> rateResponses = Arrays.asList(
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.BTC, new BigDecimal("1.2345")),
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.USD, new BigDecimal("5.12345"))
        );
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(rateResponses);
        List<CryptoCompareResponse> responses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO dto = new CryptoCompareResponseDTO(responses);

        Optional<BigDecimal> rateValue = dto.getRateValue(CryptoCompareCurrency.ETH, CryptoCompareCurrency.USD);

        assertTrue(rateValue.isPresent());
        assertEquals(new BigDecimal("5.12345"), rateValue.get());
    }

    @Test
    public void testGetRateValue_MissingToCurrency() {
        List<CryptoCompareConversionRateResponse> rateResponses = Arrays.asList(
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.BTC, new BigDecimal("1.2345"))
        );
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(rateResponses);
        List<CryptoCompareResponse> responses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO dto = new CryptoCompareResponseDTO(responses);

        Optional<BigDecimal> result1 = dto.getRateValue(CryptoCompareCurrency.ETH, CryptoCompareCurrency.USD);
        assertFalse(result1.isPresent());

        Optional<BigDecimal> result2 = dto.getRateValue(CryptoCompareCurrency.ETH, CryptoCompareCurrency.BTC);
        assertTrue(result2.isPresent());
    }

    @Test
    public void testGetRateValue_MissingFromCurrency() {
        List<CryptoCompareConversionRateResponse> rateResponses = Arrays.asList(
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.BTC, new BigDecimal("1.2345"))
        );
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(rateResponses);
        List<CryptoCompareResponse> responses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO dto = new CryptoCompareResponseDTO(responses);

        Optional<BigDecimal> result1 = dto.getRateValue(CryptoCompareCurrency.CHF, CryptoCompareCurrency.USD);
        assertFalse(result1.isPresent());
    }

}
