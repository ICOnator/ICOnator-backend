package io.iconator.kyc.service;

import io.iconator.commons.model.db.KycInfo;
import io.iconator.commons.sql.dao.KycInfoRepository;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
import io.iconator.kyc.service.exception.KycInfoNotSavedException;
import org.aspectj.apache.bcel.generic.LocalVariableGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Service
public class KycInfoService {

    private final Logger LOG = LoggerFactory.getLogger(KycInfoService.class);

    private KycInfoRepository kycInfoRepository;

    @Autowired
    public KycInfoService(KycInfoRepository kycInfoRepository) {
        assert kycInfoRepository != null;
        this.kycInfoRepository = kycInfoRepository;
    }

    public KycInfo saveKycInfo(long investorId, URI kycUri) throws KycInfoNotSavedException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            LOG.info("KycInfo for investor {} was already saved to database.", investorId);
        } else {
            KycInfo kycInfo;
            if(kycUri != null) {
                kycInfo = new KycInfo(investorId, false, kycUri.toASCIIString());
            } else {
                kycInfo = new KycInfo(investorId, false, null);
            }

            try {
                kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
                LOG.debug("Saved kycInfo in db: investorId: {}, kycUuid: {}, isKycComplete: {}", kycInfo.getInvestorId(), kycInfo.getKycUuid(), kycInfo.isKycComplete());
            } catch (DataIntegrityViolationException e) {
                LOG.info("KycInfo for investor was already saved to database.", investorId);
                kycInfoFromDb = kycInfoRepository.findById(investorId);
            } catch (Exception e) {
                LOG.error("Could not save KycInfo to database.", e);
            }
        }

        return kycInfoFromDb.orElseThrow(KycInfoNotSavedException::new);
    }

    public KycInfo setKycComplete(long investorId, boolean isKycComplete) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setKycComplete(isKycComplete);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public KycInfo setKycUri(long investorId, String kycUri) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setKycUri(kycUri);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public KycInfo setKycStartEmailSent(long investorId) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setStartKycEmailSent(true);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public KycInfo increaseNumberOfRemindersSent(long investorId) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setNoOfRemindersSent(kycInfoFromDb.get().getNoOfRemindersSent() + 1);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public KycInfo getKycInfoByInvestorId(long investorId) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    public KycInfo getKycInfoByKycUuid(UUID kycUuid) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByKycUuid(kycUuid);

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

}
