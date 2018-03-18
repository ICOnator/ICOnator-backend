package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Long> {

    Optional<Investor> findOptionalByEmail(String email);

    Optional<Investor> findOptionalByEmailConfirmationToken(String emailConfirmationToken);

    Optional<Investor> findOptionalByPayInEtherPublicKey(String payInEtherPublicKey);

    Optional<Investor> findOptionalByPayInBitcoinPublicKey(String payInBitcoinPublicKey);

    List<Investor> findAllByOrderByCreationDateAsc();

}