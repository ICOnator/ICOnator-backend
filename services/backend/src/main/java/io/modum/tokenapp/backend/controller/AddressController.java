package io.modum.tokenapp.backend.controller;

import io.modum.tokenapp.backend.controller.exceptions.*;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dao.KeyPairsRepository;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.AddressResponse;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.model.KeyPairs;
import io.modum.tokenapp.backend.service.AddressService;
import io.modum.tokenapp.backend.service.MailService;
import io.modum.tokenapp.backend.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@EnableWebMvc
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private KeyPairsRepository keyPairsRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private MailService mailService;

    public AddressController() {

    }

    @RequestMapping(value = "/address", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public AddressResponse address(@Valid @RequestBody AddressRequest addressRequest,
                                   @Valid @Size(max = Constants.UUID_CHAR_MAX_SIZE) @RequestHeader(value="Authorization") String authorizationHeader)
            throws BaseException {
        String emailConfirmationToken = getEmailConfirmationToken(authorizationHeader);
        return setWalletAddress(addressRequest, emailConfirmationToken);
    }

    @Transactional
    public AddressResponse setWalletAddress(AddressRequest addressRequest, String emailConfirmationToken)
            throws ConfirmationTokenNotFoundException, WalletAddressAlreadySetException,
            EthereumWalletAddressEmptyException, BitcoinAddressInvalidException, EthereumAddressInvalidException,
            UnexpectedException {
        // Get the user that belongs to the token
        Optional<Investor> oInvestor = findInvestorOrThrowException(emailConfirmationToken);

        // Throw if the WalletAddress is already set
        checkIfWalletAddressIsAlreadySet(oInvestor);

        // Get the addresses from the given payload
        String walletAddress = replacePrefixAddress(addressRequest.getAddress());
        String refundEthereumAddress = replacePrefixAddress(addressRequest.getRefundETH());
        String refundBitcoinAddress = addressRequest.getRefundBTC();

        // Make sure all addresses are valid and wallet address sis non-empty
        checkWalletAndRefundAddressesOrThrowException(walletAddress, refundEthereumAddress, refundBitcoinAddress);

        // Generating the keys
        long freshKeyId = keyPairsRepository.getFreshKeyID();
        KeyPairs keyPairs = keyPairsRepository.findOne(freshKeyId);

        // Persist the updated investor
        try {
            Investor investor = oInvestor.get();
            investor.setWalletAddress(addPrefixEtherIfNotExist(walletAddress))
                    .setPayInBitcoinPublicKey(keyPairs.getPublicBtc())
                    .setPayInEtherPublicKey(keyPairs.getPublicEth())
                    .setRefundBitcoinAddress(refundBitcoinAddress)
                    .setRefundEtherAddress(addPrefixEtherIfNotExist(refundEthereumAddress));
            investorRepository.save(investor);
            mailService.sendSummaryEmail(investor);
        } catch(Exception e) {
            LOG.error("Unexpected exception in AddressController: {} {}", e.getMessage(), e.getCause());
            throw new UnexpectedException();
        }

        // Return DTO
        return new AddressResponse()
                .setBtc(addressService.getBitcoinAddressFromPublicKey(keyPairs.getPublicBtc()))
                .setEther(addressService.getEthereumAddressFromPublicKey(keyPairs.getPublicEth()));
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
        if (!addressService.isValidEthereumAddress(walletAddress)) {
            throw new EthereumAddressInvalidException();
        }

        // Check if the Ethereum refund addresses are present and valid
        if (refundEthereumAddress != null
                && !refundEthereumAddress.isEmpty()
                && !addressService.isValidEthereumAddress(refundEthereumAddress)) {
            throw new EthereumAddressInvalidException();
        }
        // Check if the Bitcoin refund addresses are present and valid
        if (refundBitcoinAddress != null
                && !refundBitcoinAddress.isEmpty()
                && !addressService.isValidBitcoinAddress(refundBitcoinAddress)) {
            throw new BitcoinAddressInvalidException();
        }

    }

}
