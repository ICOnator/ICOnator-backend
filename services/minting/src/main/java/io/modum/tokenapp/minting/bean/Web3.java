package io.modum.tokenapp.minting.bean;

import io.modum.tokenapp.minting.service.ModumToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class Web3 {

    @Value("${modum.walletfile}")
    private String account;

    @Value("${modum.contract}")
    private String contract;

    @Value("${modum.walletpassword}")
    private String password;

    @Bean
    public ModumToken getModumToken() throws IOException, CipherException {
        Web3j web3j = Web3j.build(new HttpService());
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, account);
            return ModumToken.load(contract, web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
