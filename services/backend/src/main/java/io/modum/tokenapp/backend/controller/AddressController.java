package io.modum.tokenapp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    public AddressController() {

    }

    @RequestMapping("/address")
    public void register(@RequestParam("test") String test) {
        LOG.info(test);
    }

}
