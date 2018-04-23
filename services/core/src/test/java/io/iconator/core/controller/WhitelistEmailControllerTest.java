package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.core.BaseApplicationTest;
import io.iconator.core.dto.WhitelistEmailRequest;
import io.iconator.core.dto.WhitelistEmailResponse;
import io.iconator.core.service.WhitelistEmailService;
import io.iconator.core.service.exception.WhitelistEmailNotSavedException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// DirtiesContext is used to reset the context before starting any test method
// E.g., freshly restarting the h2 database
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WhitelistEmailControllerTest extends BaseApplicationTest {

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistEmailControllerTest.class);

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private WhitelistEmailService spyWhitelistEmailService;

    private MockMvc mockMvc;

    private static final String WHITELIST_SUBSCRIBE_ENDPOINT = "/whitelist/subscribe";

    public static final String TEST_EMAIL = "info@iconator.io";

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webAppContext).build();
    }

    @Test
    @Ignore
    public void testSubscribeEmail() throws Exception {
        WhitelistEmailRequest request = new WhitelistEmailRequest(TEST_EMAIL);

        MvcResult mvcResult1 = this.mockMvc.perform(post(WHITELIST_SUBSCRIBE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        WhitelistEmailResponse response1 = objectMapper.reader().forType(WhitelistEmailResponse.class)
                .readValue(mvcResult1.getResponse().getContentAsString());
        assertEquals(response1.getEmail(), request.getEmail());

        MvcResult mvcResult2 = this.mockMvc.perform(post(WHITELIST_SUBSCRIBE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        WhitelistEmailResponse response2 = objectMapper.reader().forType(WhitelistEmailResponse.class)
                .readValue(mvcResult2.getResponse().getContentAsString());
        assertEquals(response2.getEmail(), request.getEmail());

    }

    @Test(expected = WhitelistEmailNotSavedException.class)
    @Ignore
    public void testSubscribeEmail_Database_Problem() throws Exception {

        when(spyWhitelistEmailService.saveWhiteListEmail(any())).thenThrow(new WhitelistEmailNotSavedException());

        WhitelistEmailRequest request = new WhitelistEmailRequest(TEST_EMAIL);

        this.mockMvc.perform(post(WHITELIST_SUBSCRIBE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

}
