package io.iconator.backend.service;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleTierService {

    private SaleTierRepository saleTierRepository;

    @Autowired
    public SaleTierService(SaleTierRepository saleTierRepository) {
        assert saleTierRepository != null;
        this.saleTierRepository = saleTierRepository;
    }

    public List<SaleTier> getAllSaleTiersOrderdByBeginDate() {
        return saleTierRepository.findAllByOrderByBeginDateAsc();
    }
}
