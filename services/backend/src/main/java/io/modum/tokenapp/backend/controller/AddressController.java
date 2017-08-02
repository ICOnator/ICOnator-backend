package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.bean.Keys;
import io.modum.tokenapp.backend.controller.exceptions.*;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.AddressResponse;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.service.MailService;
import io.modum.tokenapp.backend.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@EnableWebMvc
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BitcoinKeyGenerator bitcoinKeyGenerator;

    @Autowired
    private EthereumKeyGenerator ethereumKeyGenerator;

    @Autowired
    private MailService mailService;

    public AddressController() {

    }

    @RequestMapping(value = "/address", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public AddressResponse address(@Valid @RequestBody AddressRequest addressRequest,
                                   @Valid @Size(max = Constants.UUID_CHAR_MAX_SIZE) @RequestHeader(value="Authorization") String authorizationHeader)
            throws BaseException {

        // TODO:
        // Instead of getting the whole raw request header, attach an AuthenticationFilter and
        // an AuthenticationProvider
        String emailConfirmationToken = getEmailConfirmationToken(authorizationHeader);
        Optional<Investor> oInvestor = findInvestorOrThrowException(emailConfirmationToken);
        checkIfWalletAddressIsAlreadySet(oInvestor);

        // Get the addresses
        String walletAddress = replacePrefixAddress(addressRequest.getAddress());
        String refundEthereumAddress = replacePrefixAddress(addressRequest.getRefundETH());
        String refundBitcoinAddress = addressRequest.getRefundBTC();

        checkWalletAndRefundAddressesOrThrowException(walletAddress, refundEthereumAddress, refundBitcoinAddress);

        // Generating the keys
        Keys bitcoinKeys = bitcoinKeyGenerator.getKeys();
        Keys ethereumKeys = ethereumKeyGenerator.getKeys();

        try {
            Investor investor = oInvestor.get();
            investor.setWalletAddress(addPrefixEtherIfNotExist(walletAddress))
                    .setPayInBitcoinAddress(bitcoinKeys.getAddressAsString())
                    .setPayInBitcoinPrivateKey(Hex.toHexString(bitcoinKeys.getPrivateKey()))
                    .setPayInEtherAddress(ethereumKeys.getAddressAsString())
                    .setPayInEtherPrivateKey(Hex.toHexString(ethereumKeys.getPrivateKey()))
                    .setRefundBitcoinAddress(refundBitcoinAddress)
                    .setRefundEtherAddress(addPrefixEtherIfNotExist(refundEthereumAddress));
            investorRepository.save(investor);
            mailService.sendSummaryEmail(investor);
        } catch(Exception e) {
            throw new UnexpectedException();
        }

        return new AddressResponse()
                .setBtc(bitcoinKeys.getAddressAsString())
                .setEther(ethereumKeys.getAddressAsString());
    }

    @RequestMapping(value = "/address/btc/{btcAddress}/validate", method = GET)
    public ResponseEntity<?> isBTCAddressValid(@Valid @Size(max = Constants.BTC_ADDRESS_CHAR_MAX_SIZE) @PathVariable("btcAddress") String btcAddress)
            throws BaseException {
        if (bitcoinKeyGenerator.isValidAddress(btcAddress)) {
            return ResponseEntity.ok().build();
        }
        throw new BitcoinAddressInvalidException();
    }

    @RequestMapping(value = "/address/eth/{ethAddress}/validate", method = GET)
    public ResponseEntity<?> isETHAddressValid(@Valid @Size(max = Constants.ETH_ADDRESS_CHAR_MAX_SIZE) @PathVariable("ethAddress") String ethAddress)
            throws BaseException {
        if (ethereumKeyGenerator.isValidAddress(ethAddress)) {
            return ResponseEntity.ok().build();
        }
        throw new EthereumAddressInvalidException();
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
        if (address == null) {
            return address;
        } else {
            return address.replaceAll("^0x", "");
        }
    }

    private String addPrefixEtherIfNotExist(String address) {
        if (address == null) {
            return address;
        } else {
            return address.startsWith("0x") ? address : ("0x" + address);
        }
    }

    private void checkIfWalletAddressIsAlreadySet(Optional<Investor> oInvestor)
            throws WalletAddressAlreadySetException {
        if (oInvestor.isPresent() && oInvestor.get().getWalletAddress() != null) {
            throw new WalletAddressAlreadySetException();
        }
    }

    private Optional<Investor> findInvestorOrThrowException(String emailConfirmationToken)
            throws ConfirmationTokenNotFoundException {
        Optional<Investor> oInvestor = investorRepository.findOptionalByEmailConfirmationToken(emailConfirmationToken);
        if (!oInvestor.isPresent()) {
            throw new ConfirmationTokenNotFoundException();
        } else {
            return oInvestor;
        }
    }

    private void checkWalletAndRefundAddressesOrThrowException(String walletAddress,
                                                               String refundEthereumAddress,
                                                               String refundBitcoinAddress)
            throws EthereumWalletAddressEmptyException, EthereumAddressInvalidException, BitcoinAddressInvalidException {

        // Check if the wallet is empty
        if (walletAddress.isEmpty()) {
            throw new EthereumWalletAddressEmptyException();
        }

        // Validate wallet address
        if (!ethereumKeyGenerator.isValidAddress(walletAddress)) {
            throw new EthereumAddressInvalidException();
        }

        // Check if the Ethereum refund addresses are present and valid
        if (refundEthereumAddress != null
                && !refundEthereumAddress.isEmpty()
                && !ethereumKeyGenerator.isValidAddress(refundEthereumAddress)) {
            throw new EthereumAddressInvalidException();
        }
        // Check if the Bitcoin refund addresses are present and valid
        if (refundBitcoinAddress != null
                && !refundBitcoinAddress.isEmpty()
                && !bitcoinKeyGenerator.isValidAddress(refundBitcoinAddress)) {
            throw new BitcoinAddressInvalidException();
        }

    }

}
