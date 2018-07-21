package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.core.dto.SaleTierResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TierControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SaleTierService saleTierService;

    @InjectMocks
    private TierController tierController;

    private JacksonTester<List<SaleTierResponse>> saleTierResponse;

    private static final String TIERS_ENDPOINT = "/tiers";

    private List<SaleTier> tiers;

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders.standaloneSetup(tierController).build();
        tiers = createFiveTiers();
    }

    @Test
    public void getAllTiers() throws Exception {

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
                saleTierResponse.write(responses).getJson());
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