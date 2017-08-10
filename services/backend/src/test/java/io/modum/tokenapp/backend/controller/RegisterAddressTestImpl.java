package io.modum.tokenapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.TokenAppBaseTest;
import io.modum.tokenapp.backend.bean.BitcoinKeyGenerator;
import io.modum.tokenapp.backend.bean.EthereumKeyGenerator;
import io.modum.tokenapp.backend.dao.InvestorRepository;
import io.modum.tokenapp.backend.dto.AddressRequest;
import io.modum.tokenapp.backend.dto.RegisterRequest;
import io.modum.tokenapp.backend.model.Investor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

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

    private ResultActions resultActions;
    private String confirmationEmailToken;

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
