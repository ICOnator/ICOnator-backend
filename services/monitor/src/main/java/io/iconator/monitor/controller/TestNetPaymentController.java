package io.iconator.monitor.controller;

import io.iconator.monitor.service.BitcoinTestPaymentService;
import io.iconator.monitor.service.EthereumTestPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@EnableWebMvc
@Profile("dev")
public class TestNetPaymentController {

    private static final Logger LOG = LoggerFactory.getLogger(TestNetPaymentController.class);

    @Autowired
    private EthereumTestPaymentService ethereumTestPaymentService;

    @Autowired
    private BitcoinTestPaymentService bitcoinTestPaymentService;

    @RequestMapping(value = "/pay/eth/{paymentToETHAddress}/{amount:.+}", method = GET)
    public ResponseEntity<?> testETHPayment(@PathVariable("paymentToETHAddress") String paymentToAddress,
                                            @PathVariable("amount") BigDecimal amount) {
        try {
            LOG.debug("Received in TestNetPaymentController API: Pay ETH to address {} with amount {}.",
                    paymentToAddress, amount.toPlainString());
            String transactionHash = ethereumTestPaymentService.pay(paymentToAddress, amount);
            return new ResponseEntity<Object>(transactionHash, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Error to pay (" + paymentToAddress + ") with amount (" +  amount + ").";
            LOG.error(errorMessage, e);
            return new ResponseEntity<Object>(errorMessage, HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(value = "/pay/btc/{paymentToBTCAddress}/{amount:.+}", method = GET)
    public ResponseEntity<?> testBTCPayment(@PathVariable("paymentToBTCAddress") String paymentToAddress,
                                               @PathVariable("amount") BigDecimal amount) {
        try {
            LOG.debug("Received in TestNetPaymentController API: Pay BTC to address {} with amount {}.",
                    paymentToAddress, amount.toPlainString());
            String transactionHash = bitcoinTestPaymentService.pay(paymentToAddress, amount);
            return new ResponseEntity<Object>(transactionHash, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Error to pay (" + paymentToAddress + ") with amount (" +  amount + ").";
            LOG.error(errorMessage, e);
            return new ResponseEntity<Object>(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

}
