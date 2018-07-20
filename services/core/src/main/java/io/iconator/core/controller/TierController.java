package io.iconator.core.controller;

import io.iconator.commons.db.services.SaleTierService;
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
        Date now = new Date();
        saleTierService.getAllSaleTiersOrderByStartDate().forEach(
                t -> {
                    SaleTierResponse.StatusType status;
                    if (t.getEndDate().compareTo(now) < 0) {
                        status = SaleTierResponse.StatusType.CLOSED;
                    } else if (t.getStartDate().compareTo(now) <= 0 &&
                            t.getEndDate().compareTo(now) > 0)  {
                        if (t.getTomicsSold().compareTo(t.getTomicsMax()) >= 0) {
                            status = SaleTierResponse.StatusType.CLOSED;
                        } else {
                            status = SaleTierResponse.StatusType.ACTIVE;
                        }
                    } else {
                        status = SaleTierResponse.StatusType.INCOMING;
                    }
                    SaleTierResponse tr = new SaleTierResponse(t.getTierNo(), t.getDescription(),
                            status, t.getStartDate(), t.getEndDate(), t.getDiscount(),
                            t.getTomicsSold(), t.getTomicsMax());
                    saleTiers.add(tr);
                });
        return ResponseEntity.ok(saleTiers);
    }
}
