package io.iconator.commons.countryfilter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CountryFilterConfigHolder {

    @Value("${io.iconator.commons.country-filter.enabled}")
    private boolean enabled;

    @Value("#{'${io.iconator.commons.country-filter.disallowed}'.split(',')}")
    private List<String> disallowedCountries;

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getDisallowedCountries() {
        return disallowedCountries;
    }
}
