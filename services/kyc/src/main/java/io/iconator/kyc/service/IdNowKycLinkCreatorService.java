package io.iconator.kyc.service;

import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IdNowKycLinkCreatorService implements KycLinkCreatorService {
    @Value("${io.iconator.kyc.host}")
    private String host;

    @Value("${io.iconator.kyc.companyId}")
    private String companyId;

    @Autowired
    private KycInfoService kycInfoService;

    @Override
    public String getKycLink(long investorId) throws InvestorNotFoundException {
        KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(investorId);

        return host + "/" + companyId + "/userdata/" + kycInfo.getKycUuid();
    }

}
