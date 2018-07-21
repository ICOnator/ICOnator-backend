package io.iconator.core.controller;

import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.core.dto.SaleTierResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
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
                t -> saleTiers.add(fromEntityToResponse(t)));

        return ResponseEntity.ok(saleTiers);
    }

    protected SaleTierResponse fromEntityToResponse(SaleTier tier) {
        SaleTierResponse response = new SaleTierResponse();
        response.setAmount(tier.getTomicsSold());
        response.setDiscount(tier.getDiscount());
        response.setEndDate(tier.getEndDate());
        response.setStartDate(tier.getStartDate());
        response.setMaxAmount(tier.getTomicsMax());
        response.setName(tier.getDescription());
        response.setTierNo(tier.getTierNo());
        response.setType(tier.getStatusAtDate(new Date()));
        return response;
    }
}
