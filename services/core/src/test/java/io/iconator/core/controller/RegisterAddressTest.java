package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.bitcoin.BitcoinKeyGenerator;
import io.iconator.commons.ethereum.EthereumKeyGenerator;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.core.BaseApplicationTest;
import io.iconator.core.dto.AddressRequest;
import io.iconator.core.dto.RegisterRequest;
import io.iconator.core.utils.Constants;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// DirtiesContext is used to reset the context before starting any test method
// E.g., freshly restarting the h2 database
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RegisterAddressTest extends BaseApplicationTest {

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

    @Value("${io.iconator.services.core.frontendWalletPath}")
    private String frontendWalletUrlPath;

    @Value("${io.iconator.commons.amqp.url}")
    private String amqpConnectionUri;

    @Rule
    public ExpectedException inetAddressExceptionRule = ExpectedException.none();

    private RegisterAddressTestImpl testImpl;

    public static final String TEST_EMAIL = "info@iconator.io";
    public static final String INVALID_CONFIRMATION_EMAIL_TOKEN = "621cb1d2-7e0c-450c-a4fd-22d0e26fdc5e";

    @Before
    public void setUp() throws Exception {
        setConnectionAndChannel(amqpConnectionUri);
        testImpl = new RegisterAddressTestImpl(this.channel)
                .createExchangeAndQueue()
                .setWebApplicationContext(webAppContext)
                .setEthereumKeyGenerator(ethereumKeyGenerator)
                .setBitcoinKeyGenerator(bitcoinKeyGenerator)
                .setInvestorRepository(investorRepository)
                .setObjectMapper(objectMapper)
                .setUpConfirmationEmailConsumer()
                .setUpSetWalletAddressMessageConsumer()
                .initializeMockMvc();
    }

    @After
    public void cleanUp() throws Exception {
        testImpl.deleteExchangeAndQueue(this.channel);
    }

    @Test
    public void testRegisterEmail() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .assertConfirmationEmailMessageConsumedMessagesSizeEquals(1);
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
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC(RegisterAddressTestImpl.generateBitcoinKey())
                                .setRefundETH(RegisterAddressTestImpl.generateEthereumKey())
                )
                .expectStatus(status().is2xxSuccessful())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(1);
    }

    @Test
    public void testEmptyRefundAddress() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .postAddress(
                        new AddressRequest()
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC("")
                                .setRefundETH("")
                )
                .expectStatus(status().isBadRequest())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(0);
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
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC("")
                                .setRefundETH("")
                )
                .expectStatus(status().isUnauthorized())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(0);;
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
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey() + "1")
                                .setRefundBTC("")
                                .setRefundETH("")
                )
                .expectStatus(status().isBadRequest())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(0);
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
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC(RegisterAddressTestImpl.generateBitcoinKey() + "11")
                                .setRefundETH("")
                )
                .expectStatus(status().isBadRequest())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(0);
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
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC("")
                                .setRefundETH(RegisterAddressTestImpl.generateEthereumKey() + "1")
                )
                .expectStatus(status().isBadRequest())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(0);
    }

    @Test
    public void testOverwritingWalletAddressWithEmailConfirmationToken() throws Exception {
        testImpl.postRegister(new RegisterRequest().setEmail(TEST_EMAIL))
                .expectStatus(status().is2xxSuccessful())
                .assertConfirmationEmailMessageConsumedMessagesSizeEquals(1)
                .fetchConfirmationEmailTokenFromDB(TEST_EMAIL)
                .getRegister()
                .expectStatus(status().is2xxSuccessful())
                .postAddress(
                        new AddressRequest()
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC(RegisterAddressTestImpl.generateBitcoinKey())
                                .setRefundETH(RegisterAddressTestImpl.generateEthereumKey())
                )
                .expectStatus(status().is2xxSuccessful())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(1)
                .postAddress(
                        new AddressRequest()
                                .setAddress(RegisterAddressTestImpl.generateEthereumKey())
                                .setRefundBTC(RegisterAddressTestImpl.generateBitcoinKey())
                                .setRefundETH(RegisterAddressTestImpl.generateEthereumKey())
                )
                .expectStatus(status().isConflict())
                .assertSetWalletAddressMessageConsumedMessagesSizeEquals(1);
    }

}
