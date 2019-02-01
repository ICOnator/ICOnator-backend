package io.iconator.core.controller;

import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.core.dto.SaleTierRequest;
import io.iconator.core.dto.SaleTierResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tiers")
public class TierController {

    private static final Logger LOG = LoggerFactory.getLogger(TierController.class);

    @Autowired
    private SaleTierService saleTierService;

    /**
     * @return HTTP response containing all sale tiers in the database ordered by  the tiers' start
     * date.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<SaleTierResponse>> getAllTiers() {

        List<SaleTierResponse> saleTiers = new ArrayList<>();
        saleTierService.getAllSaleTiersOrderByStartDate().forEach(
                t -> saleTiers.add(fromEntityToResponse(t)));

        return ResponseEntity.ok(saleTiers);
    }

    /**
     * Saves the sale tier received in the HTTP request to the database.
     * @param tiersRequest The HTTP with the sale tier to be stored/updated.
     * @return HTTP response with the just stored sale tier.
     */
    // TODO: Check for conflicting sale tier configurations (e.g. overlapping start and end dates)
    //       before inserting the received sale tier.
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<SaleTierResponse>> createTiers(
            @RequestBody List<SaleTierRequest> tiersRequest) {

        List<SaleTierResponse> tiersResponse = tiersRequest.stream().map((request) -> {
            return saleTierService.saveRequireTransaction(fromRequestToEntity(request));
        }).map((savedEntity) -> fromEntityToResponse(savedEntity)).collect(Collectors.toList());

        return ResponseEntity.ok(tiersResponse);
    }

    protected SaleTier fromRequestToEntity(SaleTierRequest request) {
        return new SaleTier(
                request.getTierNo(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDiscount(),
                request.getTomicsSold(),
                request.getTomicsMax(),
                request.getHasDynamicDuration(),
                request.getHasDynamicMax()
        );
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
        response.setType(tier.getStatusAtDate(Date.from(Instant.now())));
        return response;
    }
}
