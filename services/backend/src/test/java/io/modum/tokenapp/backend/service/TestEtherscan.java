package io.modum.tokenapp.backend.service;

import io.modum.tokenapp.backend.BackendApplication;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;


@SpringBootTest(classes = BackendApplication.class)
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class TestEtherscan extends TokenAppBaseTest {

    @Autowired
    private Etherscan etherscan;

    @Autowired
    private ExchangeRate rate;

    @Test
    public void testConnect1() {
        String balance = etherscan.getBalance("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae").toString();
        System.out.println("balance: "+balance);
    }

    @Test
    public void testConnect2() {
        String balance = etherscan.get20Balances("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae", "0x25d96310cd6694d88b9c6803be09511597c0a630").toString();
        System.out.println("balance: "+balance);
    }

    @Test
    public void testExchangeRate() throws IOException {
        String s1 = rate.getBTCUSD().toString();
        String s2 = rate.getETHUSD().toString();
        System.out.println("ret: "+s1+"/"+s2);
    }
}