package io.modum.tokenapp.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

@Service
public class Manager {

    @Autowired
    private Web3j web3j;

    public void mint() throws ExecutionException, InterruptedException {

    }

    public void deploy() {
        //Credentials credentials = WalletUtils.loadCredentials("123456", "/home/draft/.ethereum/testnet/keystore/"+ACCOUNT1);
        //ModumToken contract = ModumToken.deploy(web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, BigInteger.ZERO, new Utf8String(contractName)).get();
    }
}
