package io.iconator.core.controller;

import io.iconator.commons.amqp.model.SetWalletAddressMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinAddressService;
import io.iconator.commons.db.services.KeyPairsRepositoryService;
import io.iconator.commons.ethereum.EthereumAddressService;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.KeyPairs;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.core.controller.exceptions.*;
import io.iconator.core.dto.AddressRequest;
import io.iconator.core.dto.AddressResponse;
import io.iconator.core.utils.Constants;
import io.iconator.core.utils.IPAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Context;
import java.util.Optional;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class AddressController {

    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private KeyPairsRepositoryService keyPairsRepositoryService;

    @Autowired
    private EthereumAddressService ethereumAddressService;

    @Autowired
    private BitcoinAddressService bitcoinAddressService;

    @Autowired
    private ICOnatorMessageService messageService;

    public AddressController() {

    }

    @RequestMapping(value = "/address", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressResponse> address(@Valid @RequestBody AddressRequest addressRequest,
                                                   @Valid @Size(max = Constants.UUID_CHAR_MAX_SIZE) @RequestHeader(value = "Authorization") String authorizationHeader,
                                                   @Context HttpServletRequest requestContext)
            throws BaseException {
        // Get token
        String emailConfirmationToken = getEmailConfirmationToken(authorizationHeader);

        // Get IP address from request
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/address called from {} with token {}, address {}, refundBTC {} refundETH {}",
                ipAddress,
                emailConfirmationToken,
                addressRequest.getAddress(),
                addressRequest.getRefundBTC(),
                addressRequest.getRefundETH());

        return setWalletAddress(addressRequest, emailConfirmationToken);
    }

    private ResponseEntity<AddressResponse> setWalletAddress(AddressRequest addressRequest, String emailConfirmationToken)
            throws ConfirmationTokenNotFoundException, EthereumWalletAddressEmptyException,
            BitcoinAddressInvalidException, EthereumAddressInvalidException, AvailableKeyPairNotFoundException,
            UnexpectedException {

        // Get the user that belongs to the token
        Optional<Investor> oInvestor = findInvestorOrThrowException(emailConfirmationToken);

        // Return 409 if the WalletAddress is already set
        if (oInvestor.get().getWalletAddress() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Get refund addresses and if set check for validity
        String refundBitcoinAddress = addressRequest.getRefundBTC();
        String refundEthereumAddress = replacePrefixAddress(addressRequest.getRefundETH());
        if (refundBitcoinAddress != null && !bitcoinAddressService.isValidBitcoinAddress(refundBitcoinAddress)) {
            throw new BitcoinAddressInvalidException();
        }
        if (refundEthereumAddress != null && !ethereumAddressService.isValidEthereumAddress(refundEthereumAddress)) {
            throw new EthereumAddressInvalidException();
        }

        String walletAddress = replacePrefixAddress(addressRequest.getAddress());

        // Make sure all addresses are valid and wallet address sis non-empty
        checkWalletAndRefundAddressesOrThrowException(walletAddress, refundEthereumAddress, refundBitcoinAddress);

        // Generating the keys
        Optional<KeyPairs> oKeyPairs = keyPairsRepositoryService.getFreshKey();

        KeyPairs keyPairs = oKeyPairs.orElseThrow(() -> {
            LOG.error("Pool of addresses not initialized.");
            return new AvailableKeyPairNotFoundException();
        });

        // If it is desired, that storing the new investor is rolledback in case sending the summary email to the
        // investor or informing the monitor about the new investor fails, then a transaction has to start here.
        // The below storing and message sending code would then have to be moved into an @Transactional method
        // outside of this class.
        try {
            // Persist the updated investor
            Investor investor = oInvestor.get();
            investor.setWalletAddress(addPrefixEtherIfNotExist(walletAddress))
                    .setPayInBitcoinAddress(keyPairs.getPublicBtc())
                    .setPayInEtherAddress(keyPairs.getPublicEth())
                    .setRefundBitcoinAddress(refundBitcoinAddress)
                    .setRefundEtherAddress(addPrefixEtherIfNotExist(refundEthereumAddress));
            LOG.debug("Saving wallet for the investor (" + investor.getEmail()
                    + ") with confirmation token (" + investor.getEmailConfirmationToken() + ").");
            investorRepository.save(investor);
        } catch (Exception e) {
            LOG.error("Unexpected exception while trying to store new investor. Exception: {}", e);
            throw new UnexpectedException();
        }
        try {
            SummaryEmailMessage summaryEmailMessage = new SummaryEmailMessage(build(oInvestor.get()));
            messageService.send(summaryEmailMessage);
            SetWalletAddressMessage setWalletAddressMessage = new SetWalletAddressMessage(build(oInvestor.get()));
            messageService.send(setWalletAddressMessage);
        } catch (Exception e) {
            LOG.error("Unexpected exception while trying to queue messages. Exception:", e);
            throw new UnexpectedException();
        }

        // Return DTO
        return new ResponseEntity<>(new AddressResponse()
                .setBtc(keyPairs.getPublicBtc())
                .setEther(keyPairs.getPublicEth()), HttpStatus.OK);
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
            return null;
        } else {
            return address.replaceAll("^0x", "");
        }
    }

    private String addPrefixEtherIfNotExist(String address) {
        if (address == null) {
            return null;
        } else {
            return address.startsWith("0x") ? address : ("0x" + address);
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
        if (!ethereumAddressService.isValidEthereumAddress(walletAddress)) {
            throw new EthereumAddressInvalidException();
        }

        // Check if the Ethereum refund addresses are present and valid
        if (refundEthereumAddress != null
                && !refundEthereumAddress.isEmpty()
                && !ethereumAddressService.isValidEthereumAddress(refundEthereumAddress)) {
            throw new EthereumAddressInvalidException();
        }
        // Check if the Bitcoin refund addresses are present and valid
        if (refundBitcoinAddress != null
                && !refundBitcoinAddress.isEmpty()
                && !bitcoinAddressService.isValidBitcoinAddress(refundBitcoinAddress)) {
            throw new BitcoinAddressInvalidException();
        }

    }

}
