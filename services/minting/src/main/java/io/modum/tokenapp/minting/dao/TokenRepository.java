package io.modum.tokenapp.minting.dao;

import io.modum.tokenapp.minting.model.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByWalletAddress(String walletAddress);
    Iterable<Token> findAllByOrderByWalletAddress();
}
