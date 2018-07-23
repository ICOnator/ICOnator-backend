package io.iconator.kyc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.iconator.kyc.config.RestClientTestConfig;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.LoginResponse;
import io.iconator.kyc.service.idnow.IdNowIdentificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RestClientTestConfig.class})
@TestPropertySource({"classpath:kyc.application.properties", "classpath:application-test.properties"})
public class IdNowIdentificationServiceTest {
    private static final String TEST_AUTH_TOKEN = "1234-ABCD-efgh";

    @Value("${io.iconator.services.kyc.idnow.host}")
    private String kycHost;

    @Value("${io.iconator.services.kyc.idnow.companyId}")
    private String companyId;

    @Value("${io.iconator.services.kyc.idnow.apiKey}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IdNowIdentificationService fetcher;

    @Autowired
    private ObjectMapper objectMapper;

    private MultiValueMap<String, String> loginMap = new LinkedMultiValueMap<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Before
    public void setUp() throws Exception {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        String baseUri = kycHost + "/api/v1/" + companyId;

        loginMap.add("apiKey", apiKey);


        String loginResponse = objectMapper.writeValueAsString(new LoginResponse(TEST_AUTH_TOKEN));

        mockServer.expect(requestTo(baseUri + "/login"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(objectMapper.writeValueAsString(loginMap)))
                .andRespond(withSuccess(loginResponse, MediaType.APPLICATION_JSON_UTF8));

        Resource resource = new ClassPathResource("identifications.json");
        String jsonResponse = Files.toString(resource.getFile(), Charsets.UTF_8);
        mockServer.expect(requestTo(baseUri + "/identifications"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-LOGIN-TOKEN", TEST_AUTH_TOKEN))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testGetIdentifications() throws ParseException {
        List<Identification> identificationList = fetcher.fetchIdentifications();

        assertThat(identificationList.size()).isEqualTo(1);

        Identification identification = identificationList.get(0);
        assertThat(identification.getTransactionNumber()).isEqualTo("c02f9eea-bdef-4723-8ec3-eb254c2039f7");
        assertThat(identification.getIdentificationTime()).isEqualTo((format.parse("2014-06-02T05:03:54Z")));
        assertThat(identification.getResult()).isEqualTo("SUCCESS");
    }


}
