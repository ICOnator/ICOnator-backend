package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.bean.Keys;
import io.modum.tokenapp.backend.controller.exceptions.*;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.AddressResponse;
import io.modum.tokenapp.backend.model.Investor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
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

        // Get the addresses
        String walletAddress = replacePrefixAddress(addressRequest.getAddress());
        String refundEthereumAddress = replacePrefixAddress(addressRequest.getRefundETH());
        String refundBitcoinAddress = addressRequest.getRefundBTC();

        // Validate addresses
        if (!ethereumKeyGenerator.isValidAddress(walletAddress)
                || !ethereumKeyGenerator.isValidAddress(refundEthereumAddress)) {
            throw new EthereumAddressInvalidException();
        }
        if (!bitcoinKeyGenerator.isValidAddress(refundBitcoinAddress)) {
            throw new BitcoinAddressInvalidException();
        }

        // Generating the keys
        Keys bitcoinKeys = bitcoinKeyGenerator.getKeys();
        Keys ethereumKeys = ethereumKeyGenerator.getKeys();

        try {
            Investor investor = oInvestor.get();
            investor.setWalletAddress(walletAddress)
                    .setPayInBitcoinAddress(bitcoinKeys.getAddressAsString())
                    .setPayInBitcoinPrivateKey(Hex.toHexString(bitcoinKeys.getPrivateKey()))
                    .setPayInEtherAddress(ethereumKeys.getAddressAsString())
                    .setPayInEtherPrivateKey(Hex.toHexString(ethereumKeys.getPrivateKey()))
                    .setRefundBitcoinAddress(refundBitcoinAddress)
                    .setRefundEtherAddress(refundEthereumAddress);
            investorRepository.save(investor);
        } catch(Exception e) {
            throw new AddressException();
        }

        return new AddressResponse()
                .setBtc(bitcoinKeys.getAddressAsString())
                .setEther(ethereumKeys.getAddressAsString());
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

    private String replacePrefixAddress(String address) {
        return address.replaceAll("^0x", "");
    }

}
