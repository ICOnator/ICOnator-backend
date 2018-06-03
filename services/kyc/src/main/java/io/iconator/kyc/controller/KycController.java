package io.iconator.kyc.controller;

import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.service.InvestorService;
import io.iconator.kyc.service.KycInfoService;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
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

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
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

    @RequestMapping(value = "/kyc/{investorId}/start", method = POST)
    public ResponseEntity<?> startKyc(@PathVariable("investorId") Long investorId,
                                      @RequestBody String kycLink,
                                      @Context HttpServletRequest requestContext) {
        ResponseEntity response;
        URI kycUri;
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/start called from {} with investorId {}", ipAddress, investorId);

        try {
            kycUri = new URI(kycLink);
        } catch(URISyntaxException e) {
            return ResponseEntity.ok(kycLink + " is not a valid URI.");
        }

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            boolean isKycComplete = kycInfo.isKycComplete();
            if(!isKycComplete) {
                //KYC process with this investor started but not yet completed
                //TODO check for start email and no of emails sent
                response = ResponseEntity.ok("KYC for investor with ID " + investorId + " started but not yet complete.");
            } else {
                response = ResponseEntity.ok("KYC for investor with ID " + investorId + " already complete.");
            }
        } catch(InvestorNotFoundException e) {
            // KYC Process not yet started
            // search for investor in investor db and start process
            response = initiateKyc(investorId, kycUri);
        }

        return response;
    }

    @RequestMapping(value = "/kyc/{investorId}/complete", method = POST)
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
                response = ResponseEntity.ok("KYC of investor with ID " + investorId + " already complete.");
            } else {
                // Complete KYC
                kycInfoService.setKycComplete(investorId, true);
                response = ResponseEntity.ok("KYC of investor with ID " + investorId + " completed.");
            }
        } catch(InvestorNotFoundException e) {
            // KYC Process not yet started
            response = ResponseEntity.ok("KYC process of investor with ID " + investorId + " not yet started.");
        }

        return response;
    }

    @RequestMapping(value = "/kyc/{investorId}/status", method = GET)
    public ResponseEntity<?> getKycStatus(@PathVariable("investorId") Long investorId,
                                          @Context HttpServletRequest requestContext) {
        ResponseEntity response;
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/status called from {} with investorId {}", ipAddress, investorId);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
            response = ResponseEntity.ok(kycInfo);
        } catch(InvestorNotFoundException e) {
            LOG.info("No KYC data about investor with ID {}.", investorId);
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

    private ResponseEntity<?> initiateKyc(long investorId, URI kycUri) {
        ResponseEntity response;

        boolean investorExists = true;
        Investor investor = null;

        try {
            investor = investorService.getInvestorByInvestorId(investorId);
        } catch(InvestorNotFoundException e) {
            LOG.info("Investor with ID {} does not exist.", investorId);
            investorExists = false;
        }

        if(investorExists) {
            try {
                kycInfoService.saveKycInfo(investorId, kycUri);
                KycStartEmailMessage kycStartEmail = new KycStartEmailMessage(build(investor), kycUri.toASCIIString());
                messageService.send(kycStartEmail);
                // TODO create email templates and send email from mail-service
                response = ResponseEntity.ok("Started KYC process for investor " + investorId);
            } catch(KycInfoNotSavedException e) {
                LOG.error("KYC Info not saved.", e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

}