package io.iconator.kyc.service;

import io.iconator.kyc.dto.Identification;

import java.util.List;

public interface IdentificationService {

    List<Identification> fetchIdentifications();

}
