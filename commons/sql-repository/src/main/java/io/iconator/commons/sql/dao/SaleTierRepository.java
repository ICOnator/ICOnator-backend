package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.SaleTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.stereotype.Repository;

import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleTierRepository extends JpaRepository<SaleTier, Long>{

    Optional<SaleTier> findTierByIsActiveTrue();

    List<SaleTier> findAllByOrderByBeginDateAsc();


}
