package io.iconator.core.controller;

import io.iconator.commons.countryfilter.CountryFilterService;
import io.iconator.core.controller.exceptions.BaseException;
import io.iconator.core.dto.CountryFilterResponse;
import io.iconator.core.utils.IPAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class CountryFilterController {

    private static final Logger LOG = LoggerFactory.getLogger(CountryFilterController.class);

    @Autowired
    private CountryFilterService countryFilterService;

    @RequestMapping(value = "/countryfilter/allowed", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> isIPAllowed(HttpServletRequest requestContext) throws BaseException {

        // Get IP address from request
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        // Evaluate whether such IP is allowed or not
        boolean result = this.countryFilterService.isIPAllowed(ipAddress);
        LOG.debug("IP Address: {}, isIPAllowed result: {}", ipAddress, result);

        return new ResponseEntity<>(new CountryFilterResponse(result), HttpStatus.OK);
    }

}
