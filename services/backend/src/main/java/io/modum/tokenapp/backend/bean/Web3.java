package io.modum.tokenapp.backend.bean;

import org.springframework.context.annotation.Bean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;


public class Web3 {
    @Bean
    public Web3j getWeb3j() {
        return Web3j.build(new HttpService());
    }
}
