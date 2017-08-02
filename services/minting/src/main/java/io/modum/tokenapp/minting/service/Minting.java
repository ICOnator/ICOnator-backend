package io.modum.tokenapp.minting.service;


import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.minting.dao.TokenRepository;
import io.modum.tokenapp.minting.model.*;
import io.modum.tokenapp.rates.dao.ExchangeRateRepository;
import io.modum.tokenapp.rates.model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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

    public void mint() {
        int current = 0;
        //for(Monitoring mon:MonitoringSebi) {
            long timeStamp = 0;
            LOG.debug("processing with timestamp: {}", timeStamp);
            //current = mint(current, ...);
        //}

        for(Token token:tokenRepository.findAll()) {
            modumToken.mint(new Address(token.getWalletAddress()), new Uint256(token.getAmount()));
            LOG.debug("minting: {}:{}", token.getWalletAddress(), token.getAmount());
        }
    }

    @Transactional
    public int mint(int current, long blockNrBTC, long blockNrETH, long satoshi, long wei, String payInBTC, String payInETH) throws ExecutionException, InterruptedException {

        boolean addedBTC = false;
        boolean addedETH = false;

        if(blockNrBTC > 0 && satoshi > 0 && !payInBTC.isEmpty()) {
            ExchangeRate rate = exchangeRateRepository.findFirstByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(blockNrBTC);
            //if(rate == null) {
            //    rate = exchangeRateRepository.findFirstByOrOrderByBlockNrBtcDesc();
            //}

            BigDecimal satTmp = rate.getRateBtc().multiply(new BigDecimal(satoshi), MathContext.DECIMAL64);
            BigDecimal tokenNoDiscount = satTmp.divide(BigDecimal.TEN.pow(8), MathContext.DECIMAL64);

            Investor investor = investorRepository.findByPayInBitcoinAddress(payInBTC);
            try {
                current = calc(current, investor.getWalletAddress(), tokenNoDiscount);
                if(current >= PHASE[PHASE.length-1]) {
                    LOG.error("BTC/payIn exceeded!, BTC:{} {} {}, ETH:{} {} {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH);
                }
            } catch (ExceedingException e) {
                current = e.getCurrent();
                LOG.error("BTC/payIn partially exceeded!, BTC:{} {} {}, ETH:{} {} {}, payback tokens (USD): {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH, e.tokenNoDiscount);
            } catch (ToHighAmountException e) {
                LOG.error("BTC/payIn to high!, BTC:{} {} {}, ETH:{} {} {}, payback tokens (USD): {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH, e.tokenDiscount);
            }

            addedBTC = true;
        }

        if(blockNrETH > 0 && wei > 0 && !payInETH.isEmpty()) {
            ExchangeRate rate = exchangeRateRepository.findFirstByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(blockNrETH);
            //if(rate == null) {
            //    rate = exchangeRateRepository.findFirstByOrOrderByBlockNrEthDesc();
            //}

            BigDecimal weiTmp = rate.getRateEth().multiply(new BigDecimal(wei), MathContext.DECIMAL64);
            BigDecimal tokenNoDiscount = weiTmp.divide(BigDecimal.TEN.pow(18), MathContext.DECIMAL64);

            Investor investor = investorRepository.findByPayInEtherAddress(payInETH);
            try {
                current = calc(current, investor.getWalletAddress(), tokenNoDiscount);
                if(current > PHASE[PHASE.length-1]) {
                    LOG.error("ETH/payIn exceeded!, BTC:{} {} {}, ETH:{} {} {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH);
                }
            } catch (ExceedingException e) {
                current = e.getCurrent();
                LOG.error("ETH/payIn partially exceeded!, BTC:{} {} {}, ETH:{} {} {}, payback tokens (USD): {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH, e.tokenNoDiscount);
            } catch (ToHighAmountException e) {
                LOG.error("ETH/payIn to high!, BTC:{} {} {}, ETH:{} {} {}, payback tokens (USD): {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH, e.tokenDiscount);
            }

            addedETH = true;
        }

        if(!addedBTC && !addedETH) {
            LOG.error("payIn was wrong!, BTC:{} {} {}, ETH:{} {} {}", blockNrBTC, satoshi, payInBTC, blockNrETH, wei, payInETH);
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
