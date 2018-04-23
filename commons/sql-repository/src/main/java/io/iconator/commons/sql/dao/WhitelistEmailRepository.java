package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.model.db.WhitelistEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhitelistEmailRepository extends JpaRepository<WhitelistEmail, Long> {

    Optional<WhitelistEmail> findByEmail(String email);

}