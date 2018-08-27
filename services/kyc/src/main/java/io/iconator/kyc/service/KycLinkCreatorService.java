package io.iconator.kyc.service;


import io.iconator.commons.db.services.exception.InvestorNotFoundException;

public interface KycLinkCreatorService {

    /**
     * Automatically creates a KYC link for a specific investor.
     * @param investorId ID of investor for whom the link should be created
     * @return String of created link
     * @throws InvestorNotFoundException
     */
    String getKycLink(long investorId) throws InvestorNotFoundException;
}
