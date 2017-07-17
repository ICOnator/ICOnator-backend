package io.modum.tokenapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegisterAddressTest extends TokenAppBaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterAddressTest.class);

    public static final String REGISTER = "/register";
    public static final String ADDRESS = "/address";

    private static MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    }

    @Test
    public void testRegisterEmail() throws Exception {
        mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testConfirmationEmail() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResult.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split("/frontend/wallet/");
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken).contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testAddress() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split("/frontend/wallet/");
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken))
                .andExpect(status().is2xxSuccessful());

        MvcResult mvcResultAddress = mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress("0x1ed8cee6b63b1c6afce3ad7c92f4fd7e1b8fad9f")
                                        .setRefundBTC("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2")
                                        .setRefundETH("0x123f681646d4a755815f9cb19e1acc8565a0c2ac")
                        )
                ))
                .andExpect(status().is2xxSuccessful()).andReturn();

        LOG.info(mvcResultRegister.getResponse().getContentAsString());

    }

//    @Test
//    public void testKeyExchange_EmptyRequest() throws Exception {
//        mockMvc.perform(post(URL_KEY_EXCHANGE).contentType(APPLICATION_JSON).content("{}")).andExpect(status()
//                .is4xxClientError());
//    }

}
