package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.model.db.WhitelistEmail;
import io.iconator.core.dto.WhitelistEmailRequest;
import io.iconator.core.dto.WhitelistEmailResponse;
import io.iconator.core.service.WhitelistEmailService;
import io.iconator.core.service.exceptions.WhitelistEmailNotSavedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class WhitelistEmailControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistEmailControllerTest.class);

    @Mock
    private WhitelistEmailService mockWhitelistEmailService;

    @InjectMocks
    private WhitelistEmailController whitelistEmailController;

    private JacksonTester<WhitelistEmailResponse> jsonWhitelistEmailResponse;
    private JacksonTester<WhitelistEmailRequest> jsonWhitelistEmailRequest;

    private MockMvc mockMvc;

    private static final String WHITELIST_SUBSCRIBE_ENDPOINT = "/whitelist/subscribe";

    private static final String TEST_EMAIL = "info@iconator.io";
    private static final Date TEST_DATE = Date.from(Instant.now());

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders.standaloneSetup(whitelistEmailController).build();
    }

    @Test
    public void testSubscribeEmail() throws Exception {

        when(mockWhitelistEmailService.saveWhiteListEmail(TEST_EMAIL)).thenReturn(new WhitelistEmail(TEST_EMAIL, TEST_DATE));

        WhitelistEmailRequest request = new WhitelistEmailRequest(TEST_EMAIL);

        MvcResult mvcResult1 = this.mockMvc.perform(post(WHITELIST_SUBSCRIBE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(jsonWhitelistEmailRequest.write(request).getJson()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(mvcResult1.getResponse().getContentAsString()).isEqualTo(
                jsonWhitelistEmailResponse.write(new WhitelistEmailResponse(TEST_EMAIL, TEST_DATE)).getJson()
        );

    }

    @Test
    public void testSubscribeEmail_Database_Problem() throws Exception {

        when(mockWhitelistEmailService.saveWhiteListEmail(TEST_EMAIL)).thenThrow(new WhitelistEmailNotSavedException());

        WhitelistEmailRequest request = new WhitelistEmailRequest(TEST_EMAIL);

        this.mockMvc.perform(post(WHITELIST_SUBSCRIBE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(jsonWhitelistEmailRequest.write(request).getJson()))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

}
