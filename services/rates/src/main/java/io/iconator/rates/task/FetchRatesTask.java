package io.iconator.rates.task;

import io.iconator.rates.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchRatesTask {

    private static final Logger LOG = LoggerFactory.getLogger(FetchRatesTask.class);

    @Autowired
    private ExchangeRateService exchangeRateService;

    public FetchRatesTask() {
    }

    public void fetchRates() {
        try {
            exchangeRateService.fetchRates();
        } catch (Exception e) {
            LOG.error("Cannot fetch rates. Details: ", e);
        }
    }

}