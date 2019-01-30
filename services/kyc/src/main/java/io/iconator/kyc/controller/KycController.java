package io.iconator.kyc.controller;

import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.controller.exceptions.KycAlreadyCompletedException;
import io.iconator.kyc.controller.exceptions.KycAlreadyStartedWithEmailException;
import io.iconator.kyc.controller.exceptions.KycAlreadyStartedWithoutEmailException;
import io.iconator.kyc.controller.exceptions.KycLinkException;
import io.iconator.kyc.controller.exceptions.KycNotYetStartedException;
import io.iconator.kyc.controller.exceptions.NonexistentInvestorException;
import io.iconator.kyc.controller.exceptions.UnexpectedException;
import io.iconator.kyc.dto.CompleteSingleKycResponseDTO;
import io.iconator.kyc.dto.FetchAllResponseDTO;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.KycStartRequestDTO;
import io.iconator.kyc.dto.RemindSingleKycResponseDTO;
import io.iconator.kyc.dto.StartAllKycResponseDTO;
import io.iconator.kyc.dto.StartSingleKycResponseDTO;
import io.iconator.kyc.service.IdentificationService;
import io.iconator.kyc.service.KycInfoService;
import io.iconator.kyc.service.KycLinkCreatorService;
import io.iconator.kyc.service.exception.KycInfoNotSavedException;
import io.iconator.kyc.utils.IPAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Provides all the REST endpoints for the KYC module
 */
@RestController
public class KycController {

    private static final Logger LOG = LoggerFactory.getLogger(KycController.class);

    @Autowired
    private ICOnatorMessageService messageService;

    @Autowired
    private KycInfoService kycInfoService;

    @Autowired
    private InvestorService investorService;

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    private IdentificationService fetcher;

    @Autowired
    private KycLinkCreatorService linkCreator;

    private AmqpMessageFactory messageFactory = new AmqpMessageFactory();

    /**
     * Initiates the KYC process for all the investors in the database that have not yet started the KYC process.
     * The KYC URI gets generated automatically depending on which implementation of the {@link KycLinkCreatorService} is injected.
     * @param requestContext The context of the request that called this endpoint
     * @return DTO containing the list of investors for which KYC was started and a list of investors for which it failed
     * @throws NonexistentInvestorException if investor for which KYC is initiated does not exist
     * @throws KycLinkException if there is something wrong with the generated KYC URI
     * @throws UnexpectedException if for some reason the KYC info does not get saved
     */
    @RequestMapping(value = "/kyc/start", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StartAllKycResponseDTO> startKyc(@Context HttpServletRequest requestContext)
            throws NonexistentInvestorException, KycLinkException, UnexpectedException {
        List<Long> kycStartedList = new ArrayList<>();
        List<Long> errorList = new ArrayList<>();
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/start called from {}", ipAddress);

        List<Investor> investorList = investorService.getAllInvestors();
        List<Long> alreadySentList = kycInfoService.getAllInvestorIdWhereStartKycEmailSent();

        // initiate KYC for all investors where startKycEmail not sent and have already invested
        for(Investor investor : investorList) {
            if(!alreadySentList.contains(investor.getId())) {
                if(paymentLogService.hasInvestorInvested(investor.getId())) {
                    StartSingleKycResponseDTO initiateResponse = initiateKyc(investor.getId(), null);
                    if (initiateResponse.getIsSuccess()) {
                        kycStartedList.add(investor.getId());
                    } else {
                        LOG.error("Error while initiating KYC for investor {}", investor.getId());
                        errorList.add(investor.getId());
                    }
                }
            }
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new StartAllKycResponseDTO().setKycStartedList(kycStartedList).setErrorList(errorList));
    }

    /**
     * Initiates the KYC process for a specific investor
     * @param investorId The id of the investor for whom the KYC process is initiated
     * @param kycStartRequest An optional DTO containing the KYC URI
     * @param requestContext The context of the request that called this endpoint
     * @return A DTO containing the investor id and a boolean for success or failure
     * @throws KycAlreadyStartedWithEmailException if KYC has already been started for this investor and an email has been sent
     * @throws KycAlreadyStartedWithoutEmailException if KYC has already been started for this investor but no email has been sent
     * @throws KycAlreadyCompletedException if KYC was already completed for this investor
     * @throws KycLinkException if there is something wrong with the KYC URI
     * @throws NonexistentInvestorException if the specified investor does not exist
     * @throws UnexpectedException if for some reason the KYC info does not get saved
     */
    @RequestMapping(value = "/kyc/{investorId}/start", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StartSingleKycResponseDTO> startKycForInvestor(@PathVariable("investorId") Long investorId,
                                                                         @RequestBody(required = false) KycStartRequestDTO kycStartRequest,
                                                                         @Context HttpServletRequest requestContext)
            throws KycAlreadyStartedWithEmailException, KycAlreadyStartedWithoutEmailException,
                    KycAlreadyCompletedException, KycLinkException, NonexistentInvestorException, UnexpectedException {
        ResponseEntity<StartSingleKycResponseDTO> response;
        URI kycUri = null;
        String kycLink = kycStartRequest != null ? kycStartRequest.getKycLink() : null;
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("{}/start called from {}", investorId, ipAddress);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            boolean isKycComplete = kycInfo.isKycComplete();
            if(!isKycComplete) {
                //KYC process with this investor started but not yet completed
                if(kycInfo.isStartKycEmailSent()) {
                    throw new KycAlreadyStartedWithEmailException();
                } else {
                    throw new KycAlreadyStartedWithoutEmailException();
                }
            } else {
                throw new KycAlreadyCompletedException();
            }
        } catch(InvestorNotFoundException e) {
            // KYC Process not yet started
            // search for investor in investor db and initiate kyc
            if(kycLink != null) {
                try {
                    kycUri = new URI(kycLink);
                } catch (URISyntaxException use) {
                    throw new KycLinkException();
                }
            }
            response = ResponseEntity
                    .status(HttpStatus.OK)
                    .body(initiateKyc(investorId, kycUri));
        }

        return response;
    }

    /**
     * Sends a reminder email to the specified investor
     * @param investorId The id of the investor who should be reminded of the KYC
     * @param requestContext The context of the request that called this endpoint
     * @return A DTO containing the investor id and a boolean for success or failure
     * @throws KycAlreadyCompletedException if KYC was already completed for this investor
     * @throws KycLinkException if there is something wrong with the KYC URI
     * @throws NonexistentInvestorException if the specified investor does not exist
     * @throws KycNotYetStartedException if the KYC for the specified investor has not yet been initiated
     */
    @RequestMapping(value = "/kyc/{investorId}/remind", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RemindSingleKycResponseDTO> remindInvestor(@PathVariable("investorId") Long investorId,
                                            @Context HttpServletRequest requestContext)
            throws KycAlreadyCompletedException, KycLinkException, NonexistentInvestorException, KycNotYetStartedException {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/remind called from {} with investorId {}", ipAddress, investorId);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            if(!kycInfo.isStartKycEmailSent()) {
                throw new KycNotYetStartedException();
            }
            if(kycInfo.isKycComplete()) {
                throw new KycAlreadyCompletedException();
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(remindKyc(investorId, new URI(kycInfo.getKycUri())));

        } catch(InvestorNotFoundException e) {
            throw new NonexistentInvestorException();
        } catch(URISyntaxException e) {
            throw new KycLinkException();
        }
    }

    /**
     * Completes the KYC process for the specified investor
     * @param investorId The id of the investor for whom the KYC should be completed
     * @param requestContext The context of the request that called this endpoint
     * @return A DTO containing the investor id and a boolean for success or failure
     * @throws NonexistentInvestorException if the specified investor does not exist
     * @throws KycAlreadyCompletedException if KYC was already completed for this investor
     */
    @RequestMapping(value = "/kyc/{investorId}/complete", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CompleteSingleKycResponseDTO> setInvestorComplete(@PathVariable("investorId") Long investorId,
                                                 @Context HttpServletRequest requestContext)
            throws NonexistentInvestorException, KycAlreadyCompletedException {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/complete called from {} with investorId {}", ipAddress, investorId);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            if(kycInfo.isKycComplete()) {
                throw new KycAlreadyCompletedException();
            } else {
                // Complete KYC
                kycInfoService.setKycComplete(investorId, true);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new CompleteSingleKycResponseDTO(investorId, true));
            }
        } catch(InvestorNotFoundException e) {
            // KYC Process not yet started
            throw new NonexistentInvestorException();
        }
    }

    /**
     * Returns the status of the KYC process for the specified investor
     * @param investorId The id of the investor for whom the KYC status should be returned
     * @param requestContext The context of the request that called this endpoint
     * @return All the KYC info about the specified investor
     * @throws NonexistentInvestorException if the specified investor does not exist
     */
    @RequestMapping(value = "/kyc/{investorId}/status", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<KycInfo> getKycStatus(@PathVariable("investorId") Long investorId,
                                          @Context HttpServletRequest requestContext)
            throws NonexistentInvestorException {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/status called from {} with investorId {}", ipAddress, investorId);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(kycInfo);
        } catch(InvestorNotFoundException e) {
            throw new NonexistentInvestorException();
        }
    }

    /**
     * Fetches the KYC results from the external provider and completes the KYC process for all investors found in the data from the external provider.
     * Which external provider is used depends on which implementation of the {@link IdentificationService} is injected
     * @param requestContext The context of the request that called this endpoint
     * @return A DTO with a list of investors for which the KYC has been completed and a list of investors for which it failed
     */
    @RequestMapping(value = "/kyc/fetchall", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<FetchAllResponseDTO> fetchAllKycIdentifications(@Context HttpServletRequest requestContext) {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/fetchall called from {}", ipAddress);

        List<UUID> setCompleteList = new ArrayList<>();
        List<UUID> errorList = new ArrayList<>();

        List<Identification> identificationList = fetcher.fetchIdentifications();

        for(Identification id : identificationList) {
            if(id.getResult() != null && id.getResult().equals("SUCCESS")) {
                try {
                    UUID kycUuid = UUID.fromString(id.getId());
                    kycInfoService.setKycCompleteByUuid(kycUuid, true);
                    setCompleteList.add(kycUuid);
                } catch(InvestorNotFoundException e) {
                    LOG.info("No KYC data about investor with KYC-UUID {}.", id.getId());
                    errorList.add(UUID.fromString(id.getId()));
                }
            } else {
                //TODO what to do with differing kyc status?
            }
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new FetchAllResponseDTO().setKycCompletedList(setCompleteList).setErrorList(errorList));
    }

    private StartSingleKycResponseDTO initiateKyc(long investorId, URI kycUri) throws NonexistentInvestorException, KycLinkException, UnexpectedException {
        Investor investor;

        try {
            investor = investorService.getInvestorByInvestorId(investorId);

            kycInfoService.saveKycInfo(investorId, kycUri);
            if(kycUri == null) {
                kycUri = new URI(linkCreator.getKycLink(investorId));
                kycInfoService.setKycUri(investorId, kycUri.toASCIIString());
            }

            KycStartEmailMessage kycStartEmail = messageFactory.makeKycStartEmailMessage(investor, kycUri);
            messageService.send(kycStartEmail);
            return new StartSingleKycResponseDTO(investorId, true);
        } catch(KycInfoNotSavedException e) {
            LOG.error("KYC Info not saved.", e);
            throw new UnexpectedException();
        } catch(InvestorNotFoundException e) {
            throw new NonexistentInvestorException();
        } catch(URISyntaxException e) {
            throw new KycLinkException();
        }
    }

    private RemindSingleKycResponseDTO remindKyc(long investorId, URI kycUri) throws NonexistentInvestorException {
        Investor investor;

        try {
            investor = investorService.getInvestorByInvestorId(investorId);
            KycReminderEmailMessage kycReminderEmailMessage = messageFactory.makeKycReminderEmailMessage(investor, kycUri);
            messageService.send(kycReminderEmailMessage);
            return new RemindSingleKycResponseDTO(investorId, true);
        } catch(InvestorNotFoundException e) {
            throw new NonexistentInvestorException();
        }
    }

}