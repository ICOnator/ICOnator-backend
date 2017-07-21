package io.modum.tokenapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class RegisterAddressTest extends TokenAppBaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterAddressTest.class);

    public static final String REGISTER = "/register";
    public static final String REGISTER_CONFIRMATION_TOKEN_VALIDATE = "/register/%s/validate";
    public static final String ADDRESS = "/address";
    public static final String ADDRESS_BTC_VALIDATE = "/address/btc/%s/validate";
    public static final String ADDRESS_ETH_VALIDATE = "/address/eth/%s/validate";

    public static final String BTC_ADDRESS_VALID = "17WBW5TsRbhqCq5aeDDWR3zEpydoT3dSRB";
    public static final String BTC_ADDRESS_INVALID = "17WBW5TsRbhqCq5aeDDWR3zEpydoT3dS00"; // different last 2 chars
    public static final String ETH_ADDRESS_VALID = "0x1ed8cee6b63b1c6afce3ad7c92f4fd7e1b8fad9f";
    public static final String ETH_ADDRESS_INVALID = "0x1ed8cee6b63b1c6afce3ad7c92f4fd7e1b8fad9"; // missing the last char

    private static MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Rule
    public ExpectedException inetAddressExceptionRule = ExpectedException.none();

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
    public void testConfirmationIsValid() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResult.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split("/frontend/wallet/");
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(String.format(REGISTER_CONFIRMATION_TOKEN_VALIDATE, emailConfirmationToken))
                .contentType(APPLICATION_JSON))
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
                                        .setRefundBTC("17WBW5TsRbhqCq5aeDDWR3zEpydoT3dSRB")
                                        .setRefundETH("0x123f681646d4a755815f9cb19e1acc8565a0c2ac")
                        )
                ))
                .andExpect(status().is2xxSuccessful()).andReturn();

        LOG.info(mvcResultRegister.getResponse().getContentAsString());

    }

    @Test
    public void testIsETHAddressValid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_ETH_VALIDATE, ETH_ADDRESS_VALID)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Ignore
    // TODO: need to fix the expected value
    public void testIsETHAddressInvalid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_ETH_VALIDATE, ETH_ADDRESS_INVALID)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testIsBTCAddressValid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_BTC_VALIDATE, BTC_ADDRESS_VALID)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Ignore
    // TODO: need to fix the expected value
    public void testIsBTCAddressInvalid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_BTC_VALIDATE, BTC_ADDRESS_INVALID)))
                .andExpect(status().is5xxServerError());
    }

}
