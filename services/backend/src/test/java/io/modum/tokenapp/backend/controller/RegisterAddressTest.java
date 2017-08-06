package io.modum.tokenapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import io.modum.tokenapp.backend.utils.Constants;
import org.apache.commons.lang3.RandomStringUtils;
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

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate").contentType(APPLICATION_JSON))
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

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
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

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
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
    public void testLargeEmailAddress() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail(RandomStringUtils.randomAlphanumeric(Constants.EMAIL_CHAR_MAX_SIZE + 1)))))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testLargeEmailConfirmationTokenInURLPath() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "1/validate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLargeEmailConfirmationTokenInAuthorizationHeader() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken + "1")
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC("")
                                        .setRefundETH("")
                        )
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLargeWalletAddress() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString() + "1")
                                        .setRefundBTC("")
                                        .setRefundETH("")
                        )
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLargeRefundBTCAddress() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC(bitcoinKeyGenerator.getKeys().getAddressAsString() + "1")
                                        .setRefundETH("")
                        )
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLargeRefundETHAddress() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC("")
                                        .setRefundETH(ethereumKeyGenerator.getKeys().getAddressAsString() + "1")
                        )
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testOverwritingWalletAddressWithEmailConfirmationToken() throws Exception {
        MvcResult mvcResultRegister = mockMvc.perform(post(REGISTER).contentType(APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(new RegisterRequest().setEmail("blah@blah.org"))))
                .andExpect(status().is2xxSuccessful()).andReturn();

        String location = mvcResultRegister.getResponse().getHeaderValue("Location").toString();
        String emailConfirmationTokenSplit[] = location.split(frontendWalletUrlPath);
        String emailConfirmationToken = emailConfirmationTokenSplit[emailConfirmationTokenSplit.length - 1];

        mockMvc.perform(get(REGISTER + "/" + emailConfirmationToken + "/validate"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC(bitcoinKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundETH(ethereumKeyGenerator.getKeys().getAddressAsString())
                        )
                ))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(ADDRESS).contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + emailConfirmationToken)
                .content(
                        objectMapper.writer().writeValueAsString(
                                new AddressRequest()
                                        .setAddress(ethereumKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundBTC(bitcoinKeyGenerator.getKeys().getAddressAsString())
                                        .setRefundETH(ethereumKeyGenerator.getKeys().getAddressAsString())
                        )
                ))
                .andExpect(status().isBadRequest());

    }

}
