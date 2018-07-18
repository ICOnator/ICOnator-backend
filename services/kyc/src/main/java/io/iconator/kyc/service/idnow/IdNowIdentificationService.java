package io.iconator.kyc.service.idnow;

import io.iconator.kyc.config.KycConfigHolder;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.IdentificationResponse;
import io.iconator.kyc.dto.LoginResponse;
import io.iconator.kyc.service.IdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class IdNowIdentificationService implements IdentificationService {

    private static final Logger LOG = LoggerFactory.getLogger(IdNowIdentificationService.class);

    @Autowired
    private KycConfigHolder kycConfigHolder;

    @Autowired
    @Qualifier("restTemplateIDNow")
    private RestTemplate restTemplate;

    @Override
    public List<Identification> fetchIdentifications() {
        List<Identification> identificationList = new ArrayList<>();

        try {
            String authToken = login();
            identificationList = getIdentifications(authToken);
        } catch (URISyntaxException e) {
            LOG.error("Syntax error in URL", e);
        }

        return identificationList;
    }

    // TODO:
    // add retry mechanism -- also, being configurable
    private String login() throws URISyntaxException {
        URI uri = new URI(kycConfigHolder.getIdNowHost() + "/api/v1/" + kycConfigHolder.getIdNowCompanyId() + "/login");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("apiKey", kycConfigHolder.getIdNowApiKey());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        LoginResponse loginResponse = restTemplate.postForObject(uri, request, LoginResponse.class);

        return loginResponse.getAuthToken();
    }

    private List<Identification> getIdentifications(String authToken) throws URISyntaxException {
        URI uri = new URI(kycConfigHolder.getIdNowHost() + "/api/v1/" + kycConfigHolder.getIdNowCompanyId() + "/identifications");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("X-API-LOGIN-TOKEN", authToken);

        HttpEntity<String> request = new HttpEntity<>("parameters", headers);

        ResponseEntity<IdentificationResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, request, IdentificationResponse.class);

        return responseEntity.getBody().getIdentifications();
    }
}
