package io.iconator.kyc.service;


import io.iconator.commons.db.services.exception.InvestorNotFoundException;

public interface KycLinkCreatorService {

    String getKycLink(long investorId) throws InvestorNotFoundException;
}
