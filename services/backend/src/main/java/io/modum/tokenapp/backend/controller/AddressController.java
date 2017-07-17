package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.bean.Keys;
import io.modum.tokenapp.backend.controller.exceptions.AddressException;
import io.modum.tokenapp.backend.controller.exceptions.AuthorizationHeaderMissingException;
import io.modum.tokenapp.backend.controller.exceptions.EmailConfirmationTokenNotFoundException;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.AddressResponse;
import io.modum.tokenapp.backend.model.Investor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BitcoinKeyGenerator bitcoinKeyGenerator;

    @Autowired
    private EthereumKeyGenerator ethereumKeyGenerator;

    public AddressController() {

    }

    @RequestMapping(value = "/address", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public AddressResponse address(@Valid @RequestBody AddressRequest addressRequest,
                                     @RequestHeader(value="Authorization") String authorizationHeader)
            throws AddressException {

        // TODO:
        // Instead of getting the whole raw request header, attach an AuthenticationFilter and
        // an AuthenticationProvider
        String emailConfirmationToken = getEmailConfirmationToken(authorizationHeader);
        Optional<Investor> oInvestor = investorRepository.findOptionalByEmailConfirmationToken(emailConfirmationToken);
        if (!oInvestor.isPresent()) {
            throw new EmailConfirmationTokenNotFoundException();
        }

        // TODO:
        // Is the given addresses (walletAddress + refund ones) really valid addresses?
        // Maybe we could check that. If not valid, we could let the user know about it.
        String walletAddress = addressRequest.getAddress();
        String refundBitcoinAddress = addressRequest.getRefundBTC();
        String refundEthereumAddress = addressRequest.getRefundETH();
        Keys bitcoinKeys = bitcoinKeyGenerator.getKeys();
        Keys ethereumKeys = ethereumKeyGenerator.getKeys();

        try {
            Investor investor = oInvestor.get();
            investor.setWalletAddress(walletAddress)
                    .setPayInBitcoinAddress(bitcoinKeys.getAddressBase16())
                    .setPayInBitcoinPrivateKey(bitcoinKeys.getPrivateKeyBase16())
                    .setPayInEtherAddress(ethereumKeys.getAddressBase16())
                    .setPayInEtherPrivateKey(ethereumKeys.getPrivateKeyBase16())
                    .setRefundBitcoinAddress(refundBitcoinAddress)
                    .setRefundEtherAddress(refundEthereumAddress);
            investorRepository.save(investor);
        } catch(Exception e) {
            throw new AddressException();
        }

        return new AddressResponse()
                .setBtc(bitcoinKeys.getAddressBase16())
                .setEther(ethereumKeys.getAddressBase16());
    }

    private AddressResponse buildAddressResponse(String etherAddress, String bitcoinAddress) {
        return new AddressResponse().setEther(etherAddress).setBtc(bitcoinAddress);
    }

    private String getEmailConfirmationToken(String authorizationHeader) throws AuthorizationHeaderMissingException {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new AuthorizationHeaderMissingException();
        }

        String[] authorizationHeaderSplit = authorizationHeader.split("Bearer ");
        String emailConfirmationToken = authorizationHeaderSplit[authorizationHeaderSplit.length - 1];

        if (emailConfirmationToken == null || emailConfirmationToken.isEmpty()) {
            throw new AuthorizationHeaderMissingException();
        }

        return emailConfirmationToken;
    }

}
