package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.iconator.core.message.ConfirmationEmailMessageConsumer;
import io.iconator.core.message.SetWalletAddressMessageConsumer;
import io.iconator.commons.bitcoin.BitcoinKeyGenerator;
import io.iconator.commons.ethereum.EthereumKeyGenerator;
import io.iconator.commons.model.db.Investor;
import io.iconator.core.dto.AddressRequest;
import io.iconator.core.dto.RegisterRequest;
import io.iconator.commons.sql.dao.InvestorRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.QueueConstants.ADDRESS_SET_WALLET_QUEUE;
import static io.iconator.commons.amqp.model.constants.QueueConstants.REGISTER_CONFIRMATION_EMAIL_QUEUE;
import static io.iconator.commons.amqp.model.constants.QueueConstants.REGISTER_SUMMARY_EMAIL_QUEUE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.ADDRESS_SET_WALLET_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.REGISTER_SUMMARY_EMAIL_ROUTING_KEY;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class RegisterAddressTestImpl {

    public static final String REGISTER = "/register";
    public static final String REGISTER_CONFIRMATION_TOKEN_VALIDATE = "/register/%s/validate";
    public static final String ADDRESS = "/address";

    private static MockMvc mockMvc;

    private WebApplicationContext webApplicationContext;
    private static EthereumKeyGenerator ethereumKeyGenerator;
    private static BitcoinKeyGenerator bitcoinKeyGenerator;
    private InvestorRepository investorRepository;
    private ObjectMapper objectMapper;

    private ConfirmationEmailMessageConsumer confirmationEmailMessageConsumer;
    private SetWalletAddressMessageConsumer setWalletAddressMessageConsumer;

    private ResultActions resultActions;
    private String confirmationEmailToken;

    private Channel channel;

    public RegisterAddressTestImpl(Channel channel) {
        this.channel = channel;
    }

    public RegisterAddressTestImpl createExchangeAndQueue() throws Exception {
        channel.queueDeclare(REGISTER_CONFIRMATION_EMAIL_QUEUE, true, false, false, null);
        channel.exchangeDeclare(ICONATOR_ENTRY_EXCHANGE, "topic", true);
        channel.queueBind(REGISTER_CONFIRMATION_EMAIL_QUEUE, ICONATOR_ENTRY_EXCHANGE, REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY);

        channel.queueDeclare(REGISTER_SUMMARY_EMAIL_QUEUE, true, false, false, null);
        channel.exchangeDeclare(ICONATOR_ENTRY_EXCHANGE, "topic", true);
        channel.queueBind(REGISTER_SUMMARY_EMAIL_QUEUE, ICONATOR_ENTRY_EXCHANGE, REGISTER_SUMMARY_EMAIL_ROUTING_KEY);

        channel.queueDeclare(ADDRESS_SET_WALLET_QUEUE, true, false, false, null);
        channel.exchangeDeclare(ICONATOR_ENTRY_EXCHANGE, "topic", true);
        channel.queueBind(ADDRESS_SET_WALLET_QUEUE, ICONATOR_ENTRY_EXCHANGE, ADDRESS_SET_WALLET_ROUTING_KEY);

        return this;
    }

    public RegisterAddressTestImpl deleteExchangeAndQueue(Channel channel) throws Exception {
        channel.exchangeDelete(ICONATOR_ENTRY_EXCHANGE);
        channel.queueDelete(REGISTER_CONFIRMATION_EMAIL_QUEUE);
        channel.queueDelete(REGISTER_SUMMARY_EMAIL_QUEUE);
        channel.queueDelete(ADDRESS_SET_WALLET_QUEUE);
        return this;
    }

    public RegisterAddressTestImpl setWebApplicationContext(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
        return this;
    }

    public RegisterAddressTestImpl setEthereumKeyGenerator(EthereumKeyGenerator ethereumKeyGenerator) {
        this.ethereumKeyGenerator = ethereumKeyGenerator;
        return this;
    }

    public RegisterAddressTestImpl setBitcoinKeyGenerator(BitcoinKeyGenerator bitcoinKeyGenerator) {
        this.bitcoinKeyGenerator = bitcoinKeyGenerator;
        return this;
    }

    public RegisterAddressTestImpl setInvestorRepository(InvestorRepository investorRepository) {
        this.investorRepository = investorRepository;
        return this;
    }

    public RegisterAddressTestImpl setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    protected RegisterAddressTestImpl initializeMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        return this;
    }

    protected RegisterAddressTestImpl postRegister(RegisterRequest registerRequest) throws Exception {
        this.resultActions = mockMvc.perform(
                post(REGISTER)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(registerRequest))
        );
        return this;
    }

    protected RegisterAddressTestImpl postAddress(AddressRequest addressRequest) throws Exception {
        this.resultActions = mockMvc.perform(
                post(ADDRESS)
                        .header("Authorization", "Bearer " + this.confirmationEmailToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(addressRequest))
        );
        return this;
    }

    protected RegisterAddressTestImpl getRegister() throws Exception {
        this.resultActions = mockMvc.perform(
                get(String.format(REGISTER_CONFIRMATION_TOKEN_VALIDATE, this.confirmationEmailToken))
                        .contentType(APPLICATION_JSON)
        );
        return this;
    }

    protected RegisterAddressTestImpl fetchConfirmationEmailTokenFromDB(String email) throws Exception {
        this.confirmationEmailToken = getConfirmationEmailTokenByEmail(email);
        return this;
    }

    protected RegisterAddressTestImpl setConfirmationEmailToken(String confirmationEmailToken) throws Exception {
        this.confirmationEmailToken = confirmationEmailToken;
        return this;
    }

    protected RegisterAddressTestImpl expectStatus(ResultMatcher resultMatcher) throws Exception {
        this.resultActions = this.resultActions.andExpect(resultMatcher);
        return this;
    }

    protected RegisterAddressTestImpl setUpConfirmationEmailConsumer() throws Exception {
        confirmationEmailMessageConsumer = new ConfirmationEmailMessageConsumer();
        channel.basicConsume(REGISTER_CONFIRMATION_EMAIL_QUEUE, confirmationEmailMessageConsumer);
        return this;
    }

    protected RegisterAddressTestImpl setUpSetWalletAddressMessageConsumer() throws Exception {
        setWalletAddressMessageConsumer = new SetWalletAddressMessageConsumer();
        channel.basicConsume(ADDRESS_SET_WALLET_QUEUE, setWalletAddressMessageConsumer);
        return this;
    }

    protected RegisterAddressTestImpl assertConfirmationEmailMessageConsumedMessagesSizeEquals(int size) throws Exception {
        Thread.sleep(1000);
        assertTrue(confirmationEmailMessageConsumer.getConsumedMessages().size() == size);
        return this;
    }

    protected RegisterAddressTestImpl assertSetWalletAddressMessageConsumedMessagesSizeEquals(int size) throws Exception {
        Thread.sleep(1000);
        assertTrue(setWalletAddressMessageConsumer.getConsumedMessages().size() == size);
        return this;
    }

    protected RegisterAddressTestImpl printResult() throws Exception {
        this.resultActions.andDo(print());
        return this;
    }

    protected String getConfirmationEmailTokenByEmail(String email) throws Exception {
        Optional<Investor> oInvestor = investorRepository.findOptionalByEmail(email);
        if (oInvestor.isPresent()) {
            return oInvestor.get().getEmailConfirmationToken();
        } else {
            throw new Exception("Email not found!");
        }
    }

    protected static String generateEthereumKey() {
        return ethereumKeyGenerator.getKeys().getAddressAsString();
    }

    protected static String generateBitcoinKey() {
        return bitcoinKeyGenerator.getKeys().getAddressAsString();
    }

}
