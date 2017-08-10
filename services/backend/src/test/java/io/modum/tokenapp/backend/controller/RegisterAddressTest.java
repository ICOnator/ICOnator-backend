package io.modum.tokenapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.dao.InvestorRepository;
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
import org.springframework.web.context.WebApplicationContext;

import static io.modum.tokenapp.backend.controller.RegisterAddressTestImpl.generateBitcoinKey;
import static io.modum.tokenapp.backend.controller.RegisterAddressTestImpl.generateEthereumKey;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegisterAddressTest extends TokenAppBaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterAddressTest.class);

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private EthereumKeyGenerator ethereumKeyGenerator;

    @Autowired
    private BitcoinKeyGenerator bitcoinKeyGenerator;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${modum.tokenapp.frontendWalletPath}")
    private String frontendWalletUrlPath;

    @Rule
    public ExpectedException inetAddressExceptionRule = ExpectedException.none();

    private RegisterAddressTestImpl testImpl;

    public static final String TEST_EMAIL = "tokentest@modum.io";
    public static final String INVALID_CONFIRMATION_EMAIL_TOKEN = "621cb1d2-7e0c-450c-a4fd-22d0e26fdc5e";

    @Before
    public void setUp() {
        testImpl = new RegisterAddressTestImpl()
                .setWebApplicationContext(webAppContext)
                .setEthereumKeyGenerator(ethereumKeyGenerator)
                .setBitcoinKeyGenerator(bitcoinKeyGenerator)
                .setInvestorRepository(investorRepository)
                .setObjectMapper(objectMapper)
                .initializeMockMvc();
    }

    @Test
    public void testRegisterEmail() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful());
    }

    @Test
    public void testConfirmationIsValid() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful());
    }

    @Test
    public void testConfirmationIsInvalidValid() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .setConfirmationEmailToken(INVALID_CONFIRMATION_EMAIL_TOKEN)
                .getRegister()
                .expectStatus(status().isUnauthorized());
    }

    @Test
    public void testAddress() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC(generateBitcoinKey())
                                .setRefundETH(generateEthereumKey())
                )
                .expectStatus(status().is2xxSuccessful());
    }

    @Test
    public void testEmptyRefundAddress() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC("")
                                .setRefundETH("")
                )
                .expectStatus(status().isBadRequest());
    }

    @Test
    public void testLargeEmailAddress() throws Exception {
        testImpl.postRegister(
                    new RegisterRequest()
                        .setEmail(RandomStringUtils.randomAlphanumeric(Constants.EMAIL_CHAR_MAX_SIZE + 1)))
                .expectStatus(status().isBadRequest());
    }

    @Test
    public void testLargeEmailConfirmationTokenInURLPath() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .setConfirmationEmailToken(INVALID_CONFIRMATION_EMAIL_TOKEN + "1")
                .getRegister()
                .expectStatus(status().isUnauthorized());
    }

    @Test
    public void testLargeEmailConfirmationTokenInAuthorizationHeader() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful())
                .setConfirmationEmailToken(INVALID_CONFIRMATION_EMAIL_TOKEN + "1")
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC("")
                                .setRefundETH("")
                )
                .expectStatus(status().isUnauthorized());
    }

    @Test
    public void testLargeWalletAddress() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful())
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey() + "1")
                                .setRefundBTC("")
                                .setRefundETH("")
                )
                .expectStatus(status().isBadRequest());
    }

    @Test
    public void testLargeRefundBTCAddress() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful())
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC(generateBitcoinKey() + "1")
                                .setRefundETH("")
                )
                .expectStatus(status().isBadRequest());
    }

    @Test
    public void testLargeRefundETHAddress() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful())
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC("")
                                .setRefundETH(generateEthereumKey() + "1")
                )
                .expectStatus(status().isBadRequest());
    }

    @Test
    public void testOverwritingWalletAddressWithEmailConfirmationToken() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful())
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC(generateBitcoinKey())
                                .setRefundETH(generateEthereumKey())
                )
                .expectStatus(status().is2xxSuccessful())
                .postAddress(
                        new AddressRequest()
                                .setAddress(generateEthereumKey())
                                .setRefundBTC(generateBitcoinKey())
                                .setRefundETH(generateEthereumKey())
                )
                .expectStatus(status().isConflict());
    }

}
