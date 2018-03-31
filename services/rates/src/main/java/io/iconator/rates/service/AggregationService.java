package io.iconator.rates.service;

import io.iconator.commons.model.db.ExchangeCurrencyRate;
import io.iconator.rates.config.RatesAppConfig;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregationService {

    private final RatesAppConfig ratesAppConfig;

    @Autowired
    public AggregationService(RatesAppConfig ratesAppConfig) {
        this.ratesAppConfig = ratesAppConfig;
    }

    public BigDecimal removeOutliersAndGetMean(List<ExchangeCurrencyRate> allExchangeCurrencyRates) {
        List<Double> data = allExchangeCurrencyRates.stream().map((value) -> value.getExchangeRate().doubleValue()).collect(Collectors.toList());
        OutlierCalculation outlierCalculation = new OutlierCalculation(
                data.stream().mapToDouble((value) -> value.doubleValue()).toArray(),
                ratesAppConfig.getOutlierStdDevThresholdLowerBound(),
                ratesAppConfig.getOutlierStdDevThresholdUpperBound());
        double[] upperBoundOutlierValues = outlierCalculation.findUpperBoundOutlierValues();
        double[] lowerBoundOutlierValues = outlierCalculation.findLowerBoundOutlierValues();
        data.removeAll(Arrays.asList(upperBoundOutlierValues));
        data.removeAll(Arrays.asList(lowerBoundOutlierValues));
        return getMean(data);
    }

    private BigDecimal getMean(List<Double> data) {
        Mean mean = new Mean();
        data.stream().forEach((value) -> mean.increment(value));
        return new BigDecimal(mean.getResult());
    }

}
