package io.modum.tokenapp.minting.service;

import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.minting.MintingApplication;
import io.modum.tokenapp.minting.TokenAppBaseTest;
import io.modum.tokenapp.minting.dao.InvestorRepository;
import io.modum.tokenapp.minting.dao.TokenRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Ignore
public class TestAPI extends TokenAppBaseTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private InvestorRepository investorRepository;

    @Test
    public void testPayin() throws Exception {
        dataBtc(1, "mhHBCXiRtyaynrtbFgA3EXEnmMYa2YSVCH");
        MintingApplication runner = ctx.getBean(MintingApplication.class);
        runner.run("-p", "/tmp/test.csv");
    }

    private TestAPI dataBtc(int nr, String btc) {
        Investor i = new Investor()
                .setCreationDate(new Date())
                .setPayInBitcoinAddress(btc)
                .setWalletAddress("w"+nr)
                .setEmail("email"+nr);
        investorRepository.save(i);
        return this;
    }
}
