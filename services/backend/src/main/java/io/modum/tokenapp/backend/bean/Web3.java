package io.modum.tokenapp.backend.bean;

import io.modum.tokenapp.backend.service.ModumToken;
import org.springframework.context.annotation.Bean;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.io.IOException;
import java.math.BigInteger;


public class Web3 {

    private final static String ACCOUNT = "/home/draft/.ethereum/rinkeby/keystore/UTC--2017-07-08T13-31-28.925675418Z--25d96310cd6694d88b9c6803be09511597c0a630";
    private final static String CONTRACT = "0x9e56986293a0e54c3cb14a9df797891cc38ae2a0";

    @Bean
    public ModumToken getModumToken() throws IOException, CipherException {
        Web3j web3j = Web3j.build(new HttpService());
        Credentials credentials = WalletUtils.loadCredentials("123456", ACCOUNT);
        return ModumToken.load(CONTRACT, web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
    }
}
