package io.modum.tokenapp.backend.service;


import io.modum.tokenapp.backend.BackendApplication;
import io.modum.tokenapp.backend.dao.ExchangeRateRepository;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dao.TokenRepository;
import io.modum.tokenapp.backend.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestMinting {

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private Minting minting;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Test
    public void testMintingRate() throws ExecutionException, InterruptedException {
        dataBtc(1);
        rateBtc(99, 1);
        rateBtc(100, 1000);
        rateBtc(101, 3000);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(2000, current);
        Token token = tokenRepository.findAllByOrderByWalletAddress().iterator().next();
        Assert.assertEquals(2000, token.getAmount().intValue());
    }

    @Test
    public void testMintingRateMissing() throws ExecutionException, InterruptedException {
        dataBtc(1);
        rateBtc(99, 1);
        rateBtc(101, 3000);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(6000, current);

        Token token = tokenRepository.findAllByOrderByWalletAddress().iterator().next();
        Assert.assertEquals(6000, token.getAmount().intValue());
    }

    @Test
    public void testMintingBoundary() throws ExecutionException, InterruptedException {
        dataBtc(1).dataBtc(2);
        rateBtc(101, 1_049_999);
        rateBtc(102, 3);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(2_099_998, current);
        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC2", "");

        Assert.assertEquals(2_100_002, current);

        Iterator<Token> it = tokenRepository.findAllByOrderByWalletAddress().iterator();
        Assert.assertEquals(2_099_998,  it.next().getAmount().intValue());
        Assert.assertEquals((int) (2 + 2.85),  it.next().getAmount().intValue());
    }

    @Test
    public void testMintingBoundary2() throws ExecutionException, InterruptedException {
        dataBtc(1).dataBtc(2);
        rateBtc(101, 1_049_999);
        rateBtc(102, 8);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(2_099_998, current);
        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC2", "");

        Assert.assertEquals(2_100_010, current);

        Iterator<Token> it = tokenRepository.findAllByOrderByWalletAddress().iterator();
        Assert.assertEquals(2_099_998,  it.next().getAmount().intValue());
        Assert.assertEquals((int) (2 + 10),  it.next().getAmount().intValue());
    }

    @Test
    public void testMintingLarge() throws ExecutionException, InterruptedException {
        dataBtc(1).dataBtc(2);
        rateBtc(101, 1_050_000);
        rateBtc(102, 7);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(2_100_000, current);
        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC2", "");

        Assert.assertEquals(2_100_010, current);

        Iterator<Token> it = tokenRepository.findAllByOrderByWalletAddress().iterator();
        Assert.assertEquals(2_100_000,  it.next().getAmount().intValue());
        Assert.assertEquals((int) (10),  it.next().getAmount().intValue());
    }

    @Test
    public void testMintingLarge2() throws ExecutionException, InterruptedException {
        dataBtc(1).dataBtc(2);
        rateBtc(101, 1_050_001);
        rateBtc(102, 7);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(0, current);
        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC2", "");

        Assert.assertEquals(14, current);

        Iterator<Token> it = tokenRepository.findAllByOrderByWalletAddress().iterator();
        Assert.assertEquals(14,  it.next().getAmount().intValue());
    }

    @Test
    public void testMinting() throws ExecutionException, InterruptedException {
        dataBtc(1).dataBtc(2);
        rateBtc(101, 1_050_000);
        rateBtc(102, 1_470_000);
        rateBtc(103, 1_785_000);
        rateBtc(104, 2_100_000);
        rateBtc(105, 1);

        int current = 0;
        current = minting.mint(current, 100, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(2_100_000, current);

        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(4_200_000, current);
        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(6_300_000, current);
        current = minting.mint(current, 102, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(8_347_058, current);


        current = minting.mint(current, 103, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(10_447_058, current);
        current = minting.mint(current, 103, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(12_547_058, current);
        current = minting.mint(current, 103, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(14_564_999, current);


        current = minting.mint(current, 104, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(16_664_999, current);
        current = minting.mint(current, 104, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(18_764_999, current);
        current = minting.mint(current, 104, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(20_100_000, current);


        current = minting.mint(current, 105, 0, 1 * 100_000_000, 0, "BTC1", "");
        Assert.assertEquals(20_100_000, current);

        for(Token token:tokenRepository.findAll()) {
            System.out.println(token.getAmount()+" / "+token.getWalletAddress());
        }

    }

    private TestMinting dataBtc(int nr) {
        Investor i = new Investor()
                .setCreationDate(new Date())
                .setPayInBitcoinAddress("BTC"+nr)
                .setWalletAddress("w"+nr)
                .setEmail("email"+nr);
        investorRepository.save(i);
        return this;
    }

    private TestMinting dataEth(int nr) {
        Investor i = new Investor().setCreationDate(new Date()).setPayInBitcoinAddress("ETH"+nr).setWalletAddress("w"+nr);
        investorRepository.save(i);
        return this;
    }

    private TestMinting rateBtc(long blockNr, double rate) {
        io.modum.tokenapp.backend.model.ExchangeRate ex = new io.modum.tokenapp.backend.model.ExchangeRate();
        ex.setCreationDate(new Date()).setBlockNrBtc(blockNr).setRateBtc(BigDecimal.valueOf(rate));
        exchangeRateRepository.save(ex);
        return this;
    }

    private TestMinting rateEth(long blockNr, double rate) {
        io.modum.tokenapp.backend.model.ExchangeRate ex = new io.modum.tokenapp.backend.model.ExchangeRate();
        ex.setCreationDate(new Date()).setBlockNrEth(blockNr).setRateEth(BigDecimal.valueOf(rate));
        exchangeRateRepository.save(ex);
        return this;
    }
}
