package io.modum.tokenapp.minting.service;

import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.minting.MintingApplication;
import io.modum.tokenapp.minting.TokenAppBaseTest;
import io.modum.tokenapp.minting.dao.InvestorRepository;
import io.modum.tokenapp.minting.dao.TokenRepository;
import io.modum.tokenapp.rates.RatesApplication;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestAPI extends TokenAppBaseTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private InvestorRepository investorRepository;

    @Test
    public void testPayin() throws Exception {

        RatesApplication runner1 = ctx.getBean(RatesApplication.class);
        runner1.run("-r", "60");
        Thread.sleep(2000);

        dataBtc(1, "mhHBCXiRtyaynrtbFgA3EXEnmMYa2YSVCH");
        MintingApplication runner = ctx.getBean(MintingApplication.class);
        runner.run("-p", "/tmp/test.csv");
        runner.run("-p", "/tmp/test.csv", "-t", "/tmp/test-token.csv");
        runner.run("-t", "/tmp/test-token.csv");
    }

    private TestAPI dataBtc(int nr, String btc) {
        Investor i = new Investor()
                .setCreationDate(new Date())
                .setPayInBitcoinAddress(btc)
                .setWalletAddress(""+nr)
                .setEmail("email"+nr);
        investorRepository.save(i);
        return this;
    }
}
