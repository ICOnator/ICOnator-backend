package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.SaleTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleTierRepository extends JpaRepository<SaleTier, Long>{

    @Query(value = "select t from SaleTier t where t.startDate <= ?1 and t.endDate > ?1")
    Optional<SaleTier> findActiveTierByDate(Date date);

    List<SaleTier> findAllByOrderByStartDateAsc();

    Optional<SaleTier> findByTierNo(int tierNo);

    Optional<SaleTier> findFirstByOrderByEndDateDesc();

}
