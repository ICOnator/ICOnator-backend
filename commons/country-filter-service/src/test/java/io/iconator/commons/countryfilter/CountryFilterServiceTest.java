package io.iconator.commons.countryfilter;

import com.axlabs.ip2asn2cc.Ip2Asn2Cc;
import io.iconator.commons.countryfilter.config.CountryFilterConfig;
import io.iconator.commons.countryfilter.config.CountryFilterConfigHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CountryFilterConfig.class, CountryFilterConfigHolder.class, CountryFilterService.class})
@TestPropertySource("classpath:application-test.properties")
public class CountryFilterServiceTest {

    @MockBean
    private Ip2Asn2Cc ip2Asn2Cc;

    @SpyBean
    private CountryFilterConfigHolder countryFilterConfigHolder;

    @Autowired
    private CountryFilterService countryFilterService;

    @Test
    public void testIsIPAllowed_Correct_IP() {
        when(ip2Asn2Cc.checkIP(eq("1.2.3.4"))).thenReturn(true);
        boolean result = countryFilterService.isIPAllowed("1.2.3.4");
        assertTrue(result);
    }

    @Test
    public void testIsIPAllowed_Wrong_IP() {
        when(ip2Asn2Cc.checkIP(eq("1.2.3.4"))).thenReturn(false);
        boolean result = countryFilterService.isIPAllowed("1.2.3.4");
        assertFalse(result);
    }

    @Test
    public void testCountryFilter_Not_Enabled() {
        doReturn(false).when(countryFilterConfigHolder).isEnabled();
        boolean result = countryFilterService.isIPAllowed("1.2.3.4");
        assertTrue(result);
    }

}
