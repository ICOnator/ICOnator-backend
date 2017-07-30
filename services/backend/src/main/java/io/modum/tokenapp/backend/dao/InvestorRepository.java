package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.Investor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface InvestorRepository extends CrudRepository<Investor, Long> {

    Optional<Investor> findOptionalByEmail(String email);

    Investor findByEmail(String email);

    Investor findByPayInBitcoinAddress(String payInBtc);

    Investor findByPayInEtherAddress(String payInEth);

    Optional<Investor> findOptionalByEmailConfirmationToken(String emailConfirmationToken);

    List<Investor> findAllByOrderByCreationDateAsc();

}
