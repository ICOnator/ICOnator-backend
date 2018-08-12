package io.iconator.commons.cryptocompare;

import io.iconator.commons.cryptocompare.config.CryptoCompareConfigHolder;
import io.iconator.commons.cryptocompare.model.CryptoCompareCurrency;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
public class CryptoCompareClientService {

    private RestTemplate restTemplate;
    private CryptoCompareConfigHolder cryptoCompareConfigHolder;

    @Autowired
    public CryptoCompareClientService(@Qualifier("restTemplateCryptoCompare") RestTemplate restTemplate,
                                      CryptoCompareConfigHolder cryptoCompareConfigHolder) {
        this.restTemplate = restTemplate;
        this.cryptoCompareConfigHolder = cryptoCompareConfigHolder;
    }

    public CryptoCompareResponseDTO getHistorical(CryptoCompareCurrency fromCurrency, List<CryptoCompareCurrency> toCurrency, long timeInSecond) {
        CryptoCompareResponseDTO cryptoCompareResponseDTO = restTemplate.getForObject(
                getPriceHistoricalURI(fromCurrency, toCurrency, timeInSecond),
                CryptoCompareResponseDTO.class
        );
        return cryptoCompareResponseDTO;
    }

    protected URI getPriceHistoricalURI(CryptoCompareCurrency fromCurrency, List<CryptoCompareCurrency> toCurrency, long timeInSeconds) {
        return UriComponentsBuilder.fromUriString(cryptoCompareConfigHolder.getBaseUrl())
                .path("/data")
                .path("/pricehistorical")
                .queryParam("fsym", fromCurrency)
                .queryParam("tsyms", StringUtils.join(toCurrency, ','))
                .queryParam("ts", timeInSeconds)
                .queryParam("extraParams", "ICOnator_client")
                .build()
                .toUri();
    }

}
