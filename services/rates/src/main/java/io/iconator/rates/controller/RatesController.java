package io.iconator.rates.controller;

import io.iconator.rates.controller.dto.RatesCurrentResponse;
import io.iconator.rates.service.RatesProviderService;
import io.iconator.rates.service.exceptions.USDBTCFxException;
import io.iconator.rates.service.exceptions.USDETHFxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class RatesController {

    private static final Logger LOG = LoggerFactory.getLogger(RatesController.class);

    @Autowired
    private RatesProviderService ratesProviderService;

    @RequestMapping(value = "/rates/current", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RatesCurrentResponse> address(@Context HttpServletRequest requestContext)
            throws USDBTCFxException, USDETHFxException {
        BigDecimal latestBTCPrice = ratesProviderService.getLatestUSDPerBTC();
        BigDecimal latestETHPrice = ratesProviderService.getLatestUSDperETH();
        return ResponseEntity.ok(new RatesCurrentResponse(latestBTCPrice, latestETHPrice));
    }

}
