package io.modum.tokenapp.minting.service;


import io.modum.tokenapp.backend.service.AddressService;
import io.modum.tokenapp.minting.dao.InvestorRepository;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.minting.dao.PayinRepository;
import io.modum.tokenapp.minting.dao.TokenRepository;
import io.modum.tokenapp.minting.model.*;
import io.modum.tokenapp.rates.dao.ExchangeRateRepository;
import io.modum.tokenapp.rates.model.ExchangeRate;
import io.modum.tokenapp.rates.service.Blockr;
import io.modum.tokenapp.rates.service.Etherscan;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class Minting {

    final private static int[] PHASE = {0,
            2_100_000,
            2_100_000 + 6_000_000,
            2_100_000 + 6_000_000 + 6_000_000,
            2_100_000 + 6_000_000 + 6_000_000 + 6_000_000};
    final private static double[] PHASE_PRICE = {0.5, 0.7, 0.85, 1.0};

    private final static Logger LOG = LoggerFactory.getLogger(Minting.class);

    @Autowired
    private ModumToken modumToken;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private Etherscan etherscan;

    @Autowired
    private Blockr blockr;

    @Autowired
    private PayinRepository payinRepository;

    @Autowired
    private AddressService addressService;

    @Transactional
    public List<Payin> payin() throws ParseException, IOException {
        for(Investor investor:investorRepository.findAll()) {
            //BTC
            String payInBTC = addressService.getBitcoinAddressFromPublicKey(investor.getPayInBitcoinPublicKey());
            List<Triple<Date,Long,Long>> list =  blockr.getTxBtc(payInBTC);
            for(Triple<Date,Long,Long> t:list) {
                Payin p = new Payin();
                p.setBlockNrBtc(t.getRight());
                p.setSatoshi(t.getMiddle());
                p.setCreationDate(new Date());
                p.setTime(t.getLeft());
                p.setWalletAddress(investor.getWalletAddress());
                payinRepository.save(p);
            }
            //ETH
            String payInETH = addressService.getEthereumAddressFromPublicKey(investor.getPayInEtherPublicKey());
            list =  etherscan.getTxEth(payInETH);
            for(Triple<Date,Long,Long> t:list) {
                Payin p = new Payin();
                p.setBlockNrEth(t.getRight());
                p.setWei(t.getMiddle());
                p.setCreationDate(new Date());
                p.setTime(t.getLeft());
                p.setWalletAddress(investor.getWalletAddress());
                payinRepository.save(p);
            }
        }
        List<Payin> retVal = new ArrayList<>();
        for(Payin p:payinRepository.findAllByOrderByTimeAsc()) {
            retVal.add(p.copy());
        }
        return retVal;
    }

    @Transactional
    public List<Token> calc(List<Payin> list) throws ExecutionException, InterruptedException {
        int current = 0;
        for(Payin p:list) {
            current = mint(current, p.getBlockNrBtc(), p.getBlockNrEth(), p.getSatoshi(), p.getWei(), p.getWalletAddress());
        }
        List<Token> retVal = new ArrayList<>();
        for(Token t:tokenRepository.findAll()) {
            retVal.add(t.copy());
        }
        return retVal;
    }

    public void mint(List<Token> tokens) throws ExecutionException, InterruptedException {
        for(Token token:tokens) {
            if(modumToken == null) {
                LOG.error("no modum token bean created");
            } else {
                Future<TransactionReceipt> receipt = modumToken.mint(new Address(token.getWalletAddress()), new Uint256(token.getAmount()));
                receipt.get();
                LOG.debug("minting: {}:{}", token.getWalletAddress(), token.getAmount());
            }
        }
    }

    @Transactional
    public int mint(int current, long blockNrBTC, long blockNrETH, long satoshi, long wei, String walletAddress) throws ExecutionException, InterruptedException {

        boolean addedBTC = false;
        boolean addedETH = false;

        if(blockNrBTC > 0 && satoshi > 0) {
            ExchangeRate rate = exchangeRateRepository.findFirstByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(blockNrBTC);
            if(rate == null) {
                rate = exchangeRateRepository.findFirstByOrderByBlockNrBtcDesc();
            }

            BigDecimal satTmp = rate.getRateBtc().multiply(new BigDecimal(satoshi), MathContext.DECIMAL64);
            BigDecimal tokenNoDiscount = satTmp.divide(BigDecimal.TEN.pow(8), MathContext.DECIMAL64);


            try {
                current = calc(current, walletAddress, tokenNoDiscount);
                if(current >= PHASE[PHASE.length-1]) {
                    LOG.error("BTC/payIn exceeded!, BTC:{} {} , ETH:{} {} / {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress);
                }
            } catch (ExceedingException e) {
                current = e.getCurrent();
                LOG.error("BTC/payIn partially exceeded!, BTC:{} {}, ETH:{} {} / {}, payback tokens (USD): {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress, e.tokenNoDiscount);
            } catch (ToHighAmountException e) {
                LOG.error("BTC/payIn to high!, BTC:{} {}, ETH:{} {} / {}, payback tokens (USD): {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress, e.tokenDiscount);
            }

            addedBTC = true;
        }

        if(blockNrETH > 0 && wei > 0) {
            ExchangeRate rate = exchangeRateRepository.findFirstByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(blockNrETH);
            if(rate == null) {
                rate = exchangeRateRepository.findFirstByOrderByBlockNrEthDesc();
            }

            BigDecimal weiTmp = rate.getRateEth().multiply(new BigDecimal(wei), MathContext.DECIMAL64);
            BigDecimal tokenNoDiscount = weiTmp.divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL64);

            try {
                current = calc(current, walletAddress, tokenNoDiscount);
                if(current > PHASE[PHASE.length-1]) {
                    LOG.error("ETH/payIn exceeded!, BTC:{} {}, ETH:{} {} / {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress);
                }
            } catch (ExceedingException e) {
                current = e.getCurrent();
                LOG.error("ETH/payIn partially exceeded!, BTC:{} {}, ETH:{} {} / {}, payback tokens (USD): {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress, e.tokenNoDiscount);
            } catch (ToHighAmountException e) {
                LOG.error("ETH/payIn to high!, BTC:{} {}, ETH:{} {} / {}, payback tokens (USD): {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress, e.tokenDiscount);
            }

            addedETH = true;
        }

        if(!addedBTC && !addedETH) {
            LOG.error("payIn was wrong!, BTC:{} {}, ETH:{} {} / {}", blockNrBTC, satoshi, blockNrETH, wei, walletAddress);
        }
        return current;
    }

    private int calc(int current, String walletAddress, BigDecimal tokenNoDiscount) throws ExceedingException, ToHighAmountException {
        for(int i=0;i<PHASE.length-1;i++) {
            if (current < PHASE[i+1] && current >= PHASE[i]) {
                BigDecimal tokenDiscount = tokenNoDiscount.divide(BigDecimal.valueOf(PHASE_PRICE[i]), MathContext.DECIMAL64);
                if(tokenDiscount.intValue() > PHASE[1]) {
                    //upper limit for max. tokens is 2.1mio
                    throw new ToHighAmountException(tokenDiscount);
                }
                if (current + tokenDiscount.intValue() > PHASE[i+1]) {
                    //special case, boundaries!
                    BigDecimal totalTokens = tokenDiscount;
                    tokenDiscount = BigDecimal.valueOf(PHASE[i+1] - current);
                    tokenNoDiscount = totalTokens.subtract(tokenDiscount, MathContext.DECIMAL64).multiply(BigDecimal.valueOf(PHASE_PRICE[i]), MathContext.DECIMAL64);
                    updateMap(walletAddress, tokenDiscount.intValue());
                    current += tokenDiscount.intValue();
                    if(i == PHASE.length - 2) {
                        throw new ExceedingException(tokenNoDiscount, current);
                    }
                } else {
                    updateMap(walletAddress, tokenDiscount.intValue());
                    current += tokenDiscount.intValue();
                    return current;
                }
            }
        }
        return current;
    }

    private void updateMap(String walletAddress, int tokenDiscount) {
        Optional<Token> op = tokenRepository.findByWalletAddress(walletAddress);
        final Token token;
        if(op.isPresent()) {
            token = op.get();
        } else {
            token = new Token().setCreationDate(new Date()).setWalletAddress(walletAddress);
            tokenRepository.save(token);
        }

        Integer amount = token.getAmount();
        if(amount == null) {
            amount = 0;
        }
        amount += tokenDiscount;

        token.setAmount(amount);
    }

    public void mintFinished() {
        modumToken.mintFinished();
    }




    public class ExceedingException extends  Exception {
        final private BigDecimal tokenNoDiscount;
        final private int current;
        public ExceedingException(BigDecimal tokenNoDiscount, int current) {
            this.tokenNoDiscount = tokenNoDiscount;
            this.current = current;
        }

        public BigDecimal getTokenNoDiscount() {
            return tokenNoDiscount;
        }

        public int getCurrent() {
            return current;
        }
    }

    public class ToHighAmountException extends  Exception {
        final private BigDecimal tokenDiscount;
        public ToHighAmountException(BigDecimal tokenDiscount) {
            this.tokenDiscount = tokenDiscount;
        }

        public BigDecimal getTokenDiscount() {
            return tokenDiscount;
        }
    }
}
