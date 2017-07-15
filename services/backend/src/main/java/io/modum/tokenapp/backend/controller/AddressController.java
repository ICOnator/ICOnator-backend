package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.controller.exceptions.AddressException;
import io.modum.tokenapp.backend.controller.exceptions.RegisterException;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    public AddressController() {

    }

    @RequestMapping(value = "/address")
    public ResponseEntity<?> address(@Valid @RequestBody AddressRequest addressRequest, HttpServletResponse response)
            throws AddressException {
        return ResponseEntity.ok().build();
    }

}
