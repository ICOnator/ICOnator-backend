package io.iconator.kyc.service;

import io.iconator.kyc.service.exception.InvestorNotFoundException;

public interface KycLinkCreatorService {

    String getKycLink(long investorId) throws InvestorNotFoundException;
}
