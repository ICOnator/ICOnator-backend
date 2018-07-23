package io.iconator.kyc.service.idnow;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import io.iconator.kyc.config.KycConfigHolder;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.LoginResponse;
import io.iconator.kyc.service.IdentificationService;
import io.iconator.kyc.service.idnow.dto.IdNowIdentification;
import io.iconator.kyc.service.idnow.dto.IdNowIdentificationResponse;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Service
public class IdNowIdentificationService implements IdentificationService {

    private static final Logger LOG = LoggerFactory.getLogger(IdNowIdentificationService.class);

    @Autowired
    private KycConfigHolder kycConfigHolder;

    @Autowired
    @Qualifier("restTemplateIDNow")
    private RestTemplate restTemplate;

    @Autowired
    private Retryer retryer;

    @Override
    public List<Identification> fetchIdentifications() {
        List<Identification> identificationList = new ArrayList<>();
        String authToken = null;

        try {
            authToken = login();
        } catch (ExecutionException ee) {
            LOG.error("Error while logging in", ee.getMessage());
        } catch (RetryException re) {
            LOG.error("Reached max number of retries while logging in", re.getMessage());
        }

        if(authToken != null) {

            try {
                identificationList = getIdentifications(authToken);
            } catch (ExecutionException ee) {
                LOG.error("Error while getting identifications", ee.getMessage());
            } catch (RetryException re) {
                LOG.error("Reached maximum number of retries while getting identifications", re.getMessage());
            }

        } else {
            LOG.error("AuthToken still null after login"); //Should never happen
        }

        return identificationList;
    }

    // TODO:
    // add retry mechanism -- also, being configurable
    private String login() throws ExecutionException, RetryException {
        return (String) retryer.call(() -> {
            URI uri = new URI(kycConfigHolder.getIdNowHost() + "/api/v1/" + kycConfigHolder.getIdNowCompanyId() + "/login");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("apiKey", kycConfigHolder.getIdNowApiKey());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            LoginResponse loginResponse = restTemplate.postForObject(uri, request, LoginResponse.class);

            return loginResponse.getAuthToken();
        });
    }

    private List<Identification> getIdentifications(String authToken) throws ExecutionException, RetryException {
        return getListOfStringsFromList((List) retryer.call(() -> {
            URI uri = new URI(kycConfigHolder.getIdNowHost() + "/api/v1/" + kycConfigHolder.getIdNowCompanyId() + "/identifications");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers.set("X-API-LOGIN-TOKEN", authToken);

            HttpEntity<String> request = new HttpEntity<>("parameters", headers);

            ResponseEntity<IdNowIdentificationResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, request, IdNowIdentificationResponse.class);

            List<Identification> idList = new ArrayList<>();

            for (IdNowIdentification i : responseEntity.getBody().getIdentifications()) {
                idList.add(i.getIdNowIdentificationProcess());
            }

            return idList;
        }));
    }

    @SuppressWarnings({"unchecked"}) // can be suppressed
    private List<Identification> getListOfStringsFromList(List list) {
        if ((list != null) && (list.size() > 0)) {
            for (Object item : list) {
                if (!(item instanceof Identification)) {
                    throw new ClassCastException("List contained non-strings Elements: " + item.getClass().getCanonicalName());
                }
            }
        }
        return (List<Identification>) list;
    }
}
