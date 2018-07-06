package io.iconator.core.controller;

import io.iconator.core.dto.SaleTierResponse;
import io.iconator.core.service.SaleTierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tiers")
public class TierController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private SaleTierService saleTierService;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<SaleTierResponse>> getAllTiers() {

        List<SaleTierResponse> saleTiers = new ArrayList<>();
        saleTierService.getAllSaleTiersOrderByStartDate().forEach(
                t -> {
                    SaleTierResponse tr = new SaleTierResponse(t.getTierNo(), t.getDescription(), t.getStartDate(),
                            t.getEndDate(), t.getDiscount(), t.getTokensSold(), t.getTokenMax());
                    saleTiers.add(tr);
                });
        return new ResponseEntity<>(saleTiers, HttpStatus.OK);
    }
}
