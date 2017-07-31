package io.modum.tokenapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegisterAddressTest extends TokenAppBaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterAddressTest.class);

    public static final String REGISTER = "/register";
    public static final String REGISTER_CONFIRMATION_TOKEN_VALIDATE = "/register/%s/validate";
    public static final String ADDRESS = "/address";
    public static final String ADDRESS_BTC_VALIDATE = "/address/btc/%s/validate";
    public static final String ADDRESS_ETH_VALIDATE = "/address/eth/%s/validate";

    private static MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private EthereumKeyGenerator ethereumKeyGenerator;

    @Autowired
    private BitcoinKeyGenerator bitcoinKeyGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${modum.tokenapp.frontendWalletPath}")
    private String frontendWalletUrlPath;

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
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
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
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(String.format(REGISTER_CONFIRMATION_TOKEN_VALIDATE, emailConfirmationToken))
                .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testConfirmationIsInvalidValid() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResult.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];


        mockMvc.perform(get(String.format(REGISTER_CONFIRMATION_TOKEN_VALIDATE, emailConfirmationToken + "-error")))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testAddress() throws Exception {

        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken))
                .andExpect(status().is2xxSuccessful());

        MvcResult mvcResultAddress = mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC(bitcoinKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundETH(ethereumKeyGenerator.getKeys().getAddressAsString())
                        )
                ))
                .andExpect(status().is2xxSuccessful()).andReturn();

        LOG.info(mvcResultRegister.getResponse().getContentAsString());

    }

    @Test
    public void testEmptyRefundAddress() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken))
                .andExpect(status().is2xxSuccessful());

        MvcResult mvcResultAddress = mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC("")
                                        .setRefundETH("")
                        )
                ))
                .andExpect(status().is2xxSuccessful()).andReturn();

        LOG.info(mvcResultRegister.getResponse().getContentAsString());

    }

    @Test
    public void testIsETHAddressValid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_ETH_VALIDATE, ethereumKeyGenerator.getKeys().getAddressAsString())))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testIsETHAddressInvalid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_ETH_VALIDATE, ethereumKeyGenerator.getKeys().getAddressAsString() + "-error")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsBTCAddressValid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_BTC_VALIDATE, bitcoinKeyGenerator.getKeys().getAddressAsString())))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testIsBTCAddressInvalid() throws Exception {
        mockMvc.perform(get(String.format(ADDRESS_BTC_VALIDATE, bitcoinKeyGenerator.getKeys().getAddressAsString() + "-error")))
                .andExpect(status().isBadRequest());
    }

}
