package io.iconator.backend.controller;

import io.iconator.backend.service.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tiers")
public class TierController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    private SaleTierService saleTierService;

    @Autowired
    TierController(SaleTierService saleTierService) {
        assert saleTierService != null;
        this.saleTierService = saleTierService;
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<SaleTier>> getAllTiers() {
        return new ResponseEntity<>(saleTierService.getAllSaleTiersOrderdByBeginDate(),
                HttpStatus.OK);
    }
}
