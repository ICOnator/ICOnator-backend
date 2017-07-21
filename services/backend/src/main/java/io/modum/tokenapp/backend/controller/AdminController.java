package io.modum.tokenapp.backend.controller;


import io.modum.tokenapp.backend.controller.exceptions.RegisterException;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import io.modum.tokenapp.backend.service.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import java.util.concurrent.ExecutionException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

public class AdminController {

    @Autowired private Manager manager;

    @RequestMapping(value = "/mint", method = GET,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> mint(@Valid @RequestBody RegisterRequest registerRequest)
            throws RegisterException, ExecutionException, InterruptedException {

        manager.mint();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/mintfinished", method = GET,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> mintfinished(@Valid @RequestBody RegisterRequest registerRequest)
            throws RegisterException, ExecutionException, InterruptedException {

        manager.mintFinished();
        return ResponseEntity.ok().build();
    }
}
