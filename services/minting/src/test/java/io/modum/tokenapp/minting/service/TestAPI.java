package io.modum.tokenapp.minting.service;

import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.minting.MintingApplication;
import io.modum.tokenapp.minting.TokenAppBaseTest;
import io.modum.tokenapp.minting.dao.InvestorRepository;
import io.modum.tokenapp.rates.RatesApplication;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

        dataBtc(1, "031983465869e6cdd10899fd76240a2b105a816f26165addc597ab7e4b53ec293b",
                "031983465869e6cdd10899fd76240a2b105a816f26165addc597ab7e4b53ec293b");

        MintingApplication runner = ctx.getBean(MintingApplication.class);
        runner.run("-p", "/tmp/test.csv");
        runner.run("-p", "/tmp/test.csv", "-t", "/tmp/test-token.csv");
        runner.run("-t", "/tmp/test-token.csv");
    }

    private TestAPI dataBtc(int nr, String btc, String eth) {
        Investor i = new Investor()
                .setCreationDate(new Date())
                .setPayInBitcoinPublicKey(btc)
                .setPayInEtherPublicKey(eth)
                .setWalletAddress(""+nr)
                .setEmailConfirmationToken(UUID.randomUUID().toString())
                .setEmail("email"+nr);
        investorRepository.save(i);
        return this;
    }
}
