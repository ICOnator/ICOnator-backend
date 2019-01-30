package io.iconator.kyc.service;

import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.commons.sql.dao.KycInfoRepository;
import io.iconator.kyc.service.exception.KycInfoNotSavedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This service handles all the interaction with the KycInfo repository
 */
@Service
public class KycInfoService {

    private final Logger LOG = LoggerFactory.getLogger(KycInfoService.class);

    private KycInfoRepository kycInfoRepository;

    @Autowired
    public KycInfoService(KycInfoRepository kycInfoRepository) {
        assert kycInfoRepository != null;
        this.kycInfoRepository = kycInfoRepository;
    }

    /**
     * Saves the KYC info to the database
     * @param investorId The id of the investor
     * @param kycUri The KYC URI assigned to this investor
     * @return The KYC info that was saved to the database
     * @throws KycInfoNotSavedException if an error happened while trying to save the KYC info to the database
     */
    @Transactional
    public KycInfo saveKycInfo(long investorId, URI kycUri) throws KycInfoNotSavedException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            LOG.info("KycInfo for investor {} was already saved to database.", investorId);
        } else {
            KycInfo kycInfo;
            if(kycUri != null) {
                kycInfo = new KycInfo(investorId, false, 0, false, kycUri.toASCIIString());
            } else {
                kycInfo = new KycInfo(investorId, false, 0, false, null);
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

    /**
     * Sets the KYC process completion in the database entry for the specified investor
     * @param investorId The id of the investor
     * @param isKycComplete
     * @return The KYC info that was changed
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    @Transactional
    public KycInfo setKycComplete(long investorId, boolean isKycComplete) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setKycComplete(isKycComplete);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Sets the KYC process completion in the database entry for the specified investor
     * @param uuid UUID of the investor
     * @param isKycComplete
     * @return The KYC info that was changed
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    @Transactional
    public KycInfo setKycCompleteByUuid(UUID uuid, boolean isKycComplete) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByKycUuid(uuid);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setKycComplete(isKycComplete);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Sets the {@link KycInfo#kycUri} for the specified investor in the database
     * @param investorId The id of the investor
     * @param kycUri The KYC URI to be assigned to the investor
     * @return The KYC info that was changed
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    @Transactional
    public KycInfo setKycUri(long investorId, String kycUri) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setKycUri(kycUri);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Sets the entry for {@link KycInfo#isStartKycEmailSent} to complete
     * @param investorId The id of the investor
     * @return The KYC info that was changed
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    @Transactional
    public KycInfo setKycStartEmailSent(long investorId) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setStartKycEmailSent(true);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Increases {@link KycInfo#noOfRemindersSent} for the specified investor by one
     * @param investorId The id of the investor
     * @return The KYC info that was changed
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    @Transactional
    public KycInfo increaseNumberOfRemindersSent(long investorId) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        if(kycInfoFromDb.isPresent()) {
            KycInfo kycInfo = kycInfoFromDb.get().setNoOfRemindersSent(kycInfoFromDb.get().getNoOfRemindersSent() + 1);
            kycInfoFromDb = Optional.of(kycInfoRepository.save(kycInfo));
        }

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Returns the KYC info for the specified investor
     * @param investorId
     * @return
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    public KycInfo getKycInfoByInvestorId(long investorId) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByInvestorId(investorId);

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * Returns the KYC info for the specified investor
     * @param kycUuid
     * @return
     * @throws InvestorNotFoundException if the investor does not exist in the database
     */
    public KycInfo getKycInfoByKycUuid(UUID kycUuid) throws InvestorNotFoundException {
        Optional<KycInfo> kycInfoFromDb = kycInfoRepository.findOptionalByKycUuid(kycUuid);

        return kycInfoFromDb.orElseThrow(InvestorNotFoundException::new);
    }

    /**
     * @return A list of all investors for which the KYC start email has already been sent
     */
    public List<Long> getAllInvestorIdWhereStartKycEmailSent() {
        return kycInfoRepository.findAll().stream()
                .filter(KycInfo::isStartKycEmailSent)
                .map(KycInfo::getInvestorId)
                .collect(Collectors.toList());
    }

}
