package io.iconator.kyc.controller;

import io.iconator.commons.model.db.KycInfo;
import io.iconator.commons.sql.dao.KycInfoRepository;
import io.iconator.kyc.utils.IPAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class KycController {
    private static final Logger LOG = LoggerFactory.getLogger(KycController.class);

    @Autowired
    private KycInfoRepository kycInfoRepository;

    @RequestMapping(value = "/kyc/{investorId}/start", method = POST)
    public ResponseEntity<?> startKyc(@PathVariable("investorId") Long investorId, @Context HttpServletRequest requestContext) {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/start called from {} with investorId {}", ipAddress, investorId);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findById(investorId);
        if(oKycInfo.isPresent()) {
            boolean kycComplete = checkIfKycComplete(oKycInfo);
            if(!kycComplete) {
                // KYC Process with this customer has already been started
                // TODO check for no of emails sent
            }
        } else {
            // KYC process not yet started
            // TODO search for investor in investor db and start kyc process
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/kyc/{investorId}/complete", method = POST)
    public ResponseEntity<?> setInvestorComplete(@PathVariable("investorId") Long investorId, @Context HttpServletRequest requestContext) {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/complete called from {} with investorId {}", ipAddress, investorId);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findById(investorId);
        if(oKycInfo.isPresent()) {
            boolean kycComplete = checkIfKycComplete(oKycInfo);
            if(!kycComplete) {
                KycInfo kycInfo = oKycInfo.get().setKycComplete(true);
                kycInfoRepository.saveAndFlush(kycInfo);
                LOG.debug("Saved KYC Info to the database: investorId={}, kycComplete={}", kycInfo.getId(), kycInfo.isKycComplete());
            }
        } else {
            // Investor not already in kycInfo db
            // TODO search for investor in investor db, add to kyc db and complete
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/kyc/{investorId}/status", method = GET)
    public ResponseEntity<?> getKycStatus(@PathVariable("investorId") Long investorId, @Context HttpServletRequest requestContext) {
        String ipAddress = IPAddressUtil.getIPAddress(requestContext);

        LOG.info("/status called from {} with investorId {}", ipAddress, investorId);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findById(investorId);
        if(oKycInfo.isPresent()) {
            return ResponseEntity.ok(oKycInfo.get());
        } else {
            LOG.debug("No KYC data about investor with ID {}", investorId);
            return ResponseEntity.notFound().build();
        }

    }

    private boolean checkIfKycComplete(Optional<KycInfo> oKycInfo) {
        boolean isKycComplete = oKycInfo.get().isKycComplete();
        if(isKycComplete) {
            LOG.debug("KYC for investor with ID {} is already completed", oKycInfo.get().getId());
            return true;
        } else {
            return false;
        }
    }

    private KycInfo createKycInfo(long id, boolean isKycComplete) {
        return new KycInfo(id, isKycComplete);
    }


}
