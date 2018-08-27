package io.iconator.kyc.service;

import io.iconator.kyc.dto.Identification;

import java.util.List;

public interface IdentificationService {

    /**
     * Calls the KYC provider to fetch all identifications
     * @return List containing all the identifications
     */
    List<Identification> fetchIdentifications();

}
