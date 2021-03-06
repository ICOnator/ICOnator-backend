package io.iconator.commons.countryfilter.config;

import com.axlabs.ip2asn2cc.Ip2Asn2Cc;
import com.axlabs.ip2asn2cc.exception.RIRNotDownloadedException;
import com.axlabs.ip2asn2cc.model.FilterPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CountryFilterConfigHolder.class)
public class CountryFilterConfig {

    @ConditionalOnProperty(value = "${io.iconator.commons.country-filter.enabled}")
    @Bean
    public Ip2Asn2Cc ip2Asn2Cc(CountryFilterConfigHolder countryFilterConfigHolder) throws RIRNotDownloadedException {
        return new Ip2Asn2Cc(countryFilterConfigHolder.getDisallowCountries(), FilterPolicy.EXCLUDE_COUNTRY_CODES);
    }

}
