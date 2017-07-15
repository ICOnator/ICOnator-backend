package io.modum.tokenapp.backend.service;


import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.model.Investor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

@Service
public class Manager {

    @Autowired
    private ModumToken modumToken;

    @Autowired
    private InvestorRepository investorRepository;

    public void mint() throws ExecutionException, InterruptedException {
        for(Investor investor:investorRepository.findAll()) {
            modumToken.mint(new Address(investor.getWalletAddress()), new Uint256(1000000));
        }
    }
}
