package io.iconator.core.service;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleTierService {

    private static final Logger LOG = LoggerFactory.getLogger(SaleTierService.class);

    @Autowired
    private SaleTierRepository saleTierRepository;

    public List<SaleTier> getAllSaleTiersOrderByStartDate() {
        return saleTierRepository.findAllByOrderByStartDateAsc();
    }
}
