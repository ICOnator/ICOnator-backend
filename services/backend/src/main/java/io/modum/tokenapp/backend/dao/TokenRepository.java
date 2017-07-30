package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;


public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByWalletAddress(String walletAddress);
    Iterable<Token> findAllByOrderByWalletAddress();
}
