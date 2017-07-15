package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.controller.exceptions.AddressException;
import io.modum.tokenapp.backend.dto.AddressRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    public AddressController() {

    }

    @RequestMapping(value = "/address", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> address(@Valid @RequestBody AddressRequest addressRequest)
            throws AddressException {
        String walletAddress = addressRequest.getAddress();

        // TODO: save the address to the Investor.walletAddress
        // TODO: generate the ETH and BTC address (e.g., ethereumJ, bitcoinJ)

        return ResponseEntity.ok().build();
    }

}
