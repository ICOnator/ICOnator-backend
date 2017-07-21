package io.modum.tokenapp.backend.service;


import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.model.Investor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.concurrent.ExecutionException;

@Service
public class Manager {

    private ModumToken modumToken;

    @Autowired
    private InvestorRepository investorRepository;

    public void mint() throws ExecutionException, InterruptedException {
        for(Investor investor : investorRepository.findAll()) {
            long value = 10000;
            modumToken.mint(new Address(investor.getWalletAddress()), new Uint256(value));
        }
    }
}
