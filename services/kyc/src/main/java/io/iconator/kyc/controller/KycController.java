package io.iconator.kyc.controller;

import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.dto.FetchAllResponseDTO;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.KycStartRequestDTO;
import io.iconator.kyc.dto.StartAllKycResponseDTO;
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

    // TODO: handle errors better
    // use DTOs for everything and then throw custom exception with HttpStatus (see core for reference)

    @RequestMapping(value = "/kyc/start", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StartAllKycResponseDTO> startKyc(@Context HttpServletRequest requestContext) {
        List<Long> kycStartedList = new ArrayList<>();
        List<Long> errorList = new ArrayList<>();
        ResponseEntity response;
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/start called from {}", ipAddress);

        List<Investor> investorList = investorService.getAllInvestors();
        List<Long> alreadySentList = kycInfoService.getAllInvestorIdWhereStartKycEmailSent();

        // initiate KYC for all investors where startKycEmail not sent and have already invested
        for(Investor investor : investorList) {
            if(!alreadySentList.contains(investor.getId())) {
                if(paymentLogService.hasInvestorInvested(investor.getId())) {
                    response = initiateKyc(investor.getId(), null);
                    if (response.getStatusCode() == HttpStatus.OK) {
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

    @RequestMapping(value = "/kyc/{investorId}/start", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> startKycForInvestor(@PathVariable("investorId") Long investorId,
                                                 @RequestBody(required = false) KycStartRequestDTO kycStartRequest,
                                                 @Context HttpServletRequest requestContext) {
        ResponseEntity response;
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
                    // setting link & sending reminder
                    if(kycLink == null) {
                        kycLink = kycInfo.getKycUri();
                    }

                    try {
                        kycUri = new URI(kycLink);
                    } catch(URISyntaxException e) {
                        return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(e.getMessage());
                    }

                    response = remindKyc(investorId, kycUri);
                } else {
                    response = ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("KYC for investor with ID " + investorId + " started but start email not yet sent.");
                    // TODO: resend start email
                }
            } else {
                response = ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("KYC for investor with ID " + investorId + " already complete.");
            }
        } catch(InvestorNotFoundException e) {
            // KYC Process not yet started
            // search for investor in investor db and start process
            if(kycLink != null) {
                try {
                    kycUri = new URI(kycLink);
                } catch (URISyntaxException use) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(use.getMessage());
                }
            }

            response = initiateKyc(investorId, kycUri);
        }

        return response;
    }

    @RequestMapping(value = "/kyc/{investorId}/complete", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> setInvestorComplete(@PathVariable("investorId") Long investorId,
                                                 @Context HttpServletRequest requestContext) {
        ResponseEntity response;
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/complete called from {} with investorId {}", ipAddress, investorId);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            boolean isKycComplete = kycInfo.isKycComplete();
            if(isKycComplete) {
                // KYC is already complete
                response = ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("KYC of investor with ID " + investorId + " already complete.");
            } else {
                // Complete KYC
                kycInfoService.setKycComplete(investorId, true);
                response = ResponseEntity
                        .status(HttpStatus.OK)
                        .body("KYC status of investor with ID " + investorId + " set to completed.");
            }
        } catch(InvestorNotFoundException e) {
            // KYC Process not yet started
            response = ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return response;
    }

    @RequestMapping(value = "/kyc/{investorId}/status", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getKycStatus(@PathVariable("investorId") Long investorId,
                                          @Context HttpServletRequest requestContext) {
        ResponseEntity response;
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/status called from {} with investorId {}", ipAddress, investorId);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            response = ResponseEntity
                    .status(HttpStatus.OK)
                    .body(kycInfo);
        } catch(InvestorNotFoundException e) {
            LOG.info("No KYC data about investor with ID {}.", investorId);
            response = ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return response;
    }

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

    private ResponseEntity<?> initiateKyc(long investorId, URI kycUri) {
        ResponseEntity response;
        Investor investor;

        try {
            investor = investorService.getInvestorByInvestorId(investorId);
        } catch(InvestorNotFoundException e) {
            LOG.info("Investor with ID {} does not exist.", investorId);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        try {
            kycInfoService.saveKycInfo(investorId, kycUri);
            if(kycUri == null) {
                try {
                    kycUri = new URI(linkCreator.getKycLink(investorId));
                    kycInfoService.setKycUri(investorId, kycUri.toASCIIString());
                } catch(Exception e) {
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(e.getMessage());
                }
            }
            KycStartEmailMessage kycStartEmail = messageFactory.makeKycStartEmailMessage(investor, kycUri);
            messageService.send(kycStartEmail);
            response = ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Started KYC process for investor " + investorId);
        } catch(KycInfoNotSavedException e) {
            LOG.error("KYC Info not saved.", e);
            response = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }

        return response;
    }

    private ResponseEntity<?> remindKyc(long investorId, URI kycUri) {
        ResponseEntity response;
        Investor investor;

        try {
            investor = investorService.getInvestorByInvestorId(investorId);

            KycReminderEmailMessage kycReminderEmailMessage = messageFactory.makeKycReminderEmailMessage(investor, kycUri);

            messageService.send(kycReminderEmailMessage);

            response = ResponseEntity
                    .status(HttpStatus.OK)
                    .body("KYC for investor with ID " + investorId + " started but not yet complete. Sending Reminder.");
        } catch(InvestorNotFoundException e) {
            LOG.info("Investor with ID {} does not exist.", investorId);
            response = ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return response;
    }

}