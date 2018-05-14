package io.iconator.commons.countryfilter;

import com.axlabs.ip2asn2cc.Ip2Asn2Cc;
import io.iconator.commons.countryfilter.config.CountryFilterConfigHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountryFilterService {

    @Autowired(required = false)
    private Ip2Asn2Cc ip2Asn2Cc;

    @Autowired
    private CountryFilterConfigHolder countryFilterConfigHolder;

    public boolean isIPAllowed(String ipAddress) {
        return countryFilterConfigHolder.isEnabled() ? this.ip2Asn2Cc.checkIP(ipAddress) : true;
    }

}
