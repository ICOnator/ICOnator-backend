package io.modum.tokenapp.minting.dao;

import io.modum.tokenapp.backend.model.Investor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface InvestorRepository extends CrudRepository<Investor, Long> {
}
