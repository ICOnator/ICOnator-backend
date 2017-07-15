package io.modum.tokenapp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    public RegisterController() {

    }

    @RequestMapping("/register")
    public void register(@RequestParam("test") String test) {
        LOG.info(test);
    }

}
