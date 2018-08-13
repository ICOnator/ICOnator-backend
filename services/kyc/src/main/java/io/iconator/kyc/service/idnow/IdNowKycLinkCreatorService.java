package io.iconator.kyc.service.idnow;

import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.config.KycConfigHolder;
import io.iconator.kyc.service.KycInfoService;
import io.iconator.kyc.service.KycLinkCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdNowKycLinkCreatorService implements KycLinkCreatorService {

    @Autowired
    private KycConfigHolder kycConfigHolder;

    @Autowired
    private KycInfoService kycInfoService;

    @Override
    public String getKycLink(long investorId) throws InvestorNotFoundException {
        KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);
        return kycConfigHolder.getIdNowHost() + "/" + kycConfigHolder.getIdNowCompanyId() + "/userdata/" + kycInfo.getKycUuid();
    }

}
