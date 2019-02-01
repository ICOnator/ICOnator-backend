package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.auth.ActuatorAuthSecurityConfig;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.core.dto.SaleTierRequest;
import io.iconator.core.dto.SaleTierResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ActuatorAuthSecurityConfig.class, FilterChainProxy.class})
@TestPropertySource({"classpath:core.application.properties", "classpath:application-test.properties"})
public class TierControllerTest {

    private MockMvc mockMvc;

    @Autowired
    FilterChainProxy springSecurityFilterChain;

    @Mock
    private SaleTierService saleTierService;

    @InjectMocks
    private TierController tierController;

    private static final String TIERS_ENDPOINT = "/tiers";
    private static final String TIERS_CREATE_ENDPOINT = "/tiers/create";

    private List<SaleTier> tiers;

    private JacksonTester<List<SaleTierRequest>> jsonSaleTierRequest;
    private JacksonTester<List<SaleTierResponse>> jsonSaleTierResponse;

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders.standaloneSetup(tierController).apply(springSecurity(springSecurityFilterChain)).build();
    }

    @Test
    public void getAllTiers() throws Exception {
        tiers = createFiveTiers();
        when(saleTierService.getAllSaleTiersOrderByStartDate())
                .thenReturn(tiers);
        MvcResult result = this.mockMvc.perform(get(TIERS_ENDPOINT)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        List<SaleTierResponse> responses = tiers.stream()
                .map(t -> tierController.fromEntityToResponse(t))
                .collect(Collectors.toList());

        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                jsonSaleTierResponse.write(responses).getJson());
    }

    @Test
    public void createTiers_With_Right_Credential() throws Exception {
        when(saleTierService.getAllSaleTiersOrderByStartDate())
                .thenReturn(tiers);

        Instant now = Instant.now();
        SaleTierRequest req1 = new SaleTierRequest(1, "test1",
                Date.from(now), Date.from(now.plusSeconds(6000)),
                new BigDecimal("0.5"), new BigInteger("0"), new BigInteger("1000000000"),
                true, true);
        List<SaleTierRequest> listRequest = Arrays.asList(req1);
        SaleTier saleTierEntityRequest = tierController.fromRequestToEntity(req1);

        when(saleTierService.saveRequireTransaction(any())).thenReturn(saleTierEntityRequest);

        MvcResult result = this.mockMvc.perform(post(TIERS_CREATE_ENDPOINT)
                .with(httpBasic("user", "password"))
                .content(jsonSaleTierRequest.write(listRequest).getJson())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        SaleTierResponse resp1 = new SaleTierResponse(1, "test1", SaleTier.StatusType.ACTIVE,
                Date.from(now), Date.from(now.plusSeconds(6000)),
                new BigDecimal("0.5"), new BigInteger("0"), new BigInteger("1000000000"));
        List<SaleTierResponse> listResponse = Arrays.asList(resp1);

        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                jsonSaleTierResponse.write(listResponse).getJson());
    }

    @Test
    public void createTiers_With_Wrong_Credential() throws Exception {
        Instant now = Instant.now();
        SaleTierRequest req1 = new SaleTierRequest(1, "test1",
                Date.from(now), Date.from(now.plusSeconds(6000)),
                new BigDecimal("0.5"), new BigInteger("0"), new BigInteger("1000000000"),
                true, true);
        List<SaleTierRequest> listRequest = Arrays.asList(req1);

        this.mockMvc.perform(post(TIERS_CREATE_ENDPOINT)
                .with(httpBasic("user", "wrongpass"))
                .content(jsonSaleTierRequest.write(listRequest).getJson())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
    }

    private List<SaleTier> createFiveTiers() {
        List<SaleTier> tiers = new ArrayList<>();
        tiers.add(createTier(0, "2018-08-01", "2018-09-01", new BigDecimal("0.25"),
                new BigInteger("47777126400000000000000000"), false, false));

        tiers.add(createTier(1, "2018-09-01", "2018-10-01", new BigDecimal("0.25"),
                new BigInteger("5972140800000000000000000"), true, false));

        tiers.add(createTier(2, "2018-10-01", "2018-10-02", new BigDecimal("0.20"),
                BigInteger.ZERO, true, true));

        tiers.add(createTier(3, "2018-10-02", "2018-10-09", new BigDecimal("0.15"),
                BigInteger.ZERO, true, true));

        tiers.add(createTier(4, "2018-10-09", "2018-10-16", new BigDecimal("0.10"),
                BigInteger.ZERO, true, true));

        tiers.add(createTier(5, "2018-10-16", "2018-10-23", BigDecimal.ZERO,
                BigInteger.ZERO, true, true));
        return tiers;
    }

    private SaleTier createTier(int tierNo, String startDate, String endDate, BigDecimal discount,
                                BigInteger tomicsMax, boolean hasDynamicDuration,
                                boolean hasDynamicMax) {
        return new SaleTier(
                tierNo,
                "test tier " + tierNo,
                Date.valueOf(startDate),
                Date.valueOf(endDate),
                discount,
                BigInteger.ZERO,
                tomicsMax,
                hasDynamicDuration,
                hasDynamicMax);
    }
}