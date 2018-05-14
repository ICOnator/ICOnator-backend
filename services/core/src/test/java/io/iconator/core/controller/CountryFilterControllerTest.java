package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.countryfilter.CountryFilterService;
import io.iconator.core.dto.CountryFilterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class CountryFilterControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(CountryFilterControllerTest.class);

    @Mock
    private CountryFilterService mockCountryFilterService;

    @InjectMocks
    private CountryFilterController countryFilterController;

    private JacksonTester<CountryFilterResponse> jsonCountryFilterResponse;

    private MockMvc mockMvc;

    private static final String COUNTRY_FILTER_ALLOWED_ENDPOINT = "/countryfilter/allowed";

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders.standaloneSetup(countryFilterController).build();
    }

    @Test
    public void testCountryFilterAllowed_with_X_Real_IP_Header() throws Exception {

        when(mockCountryFilterService.isIPAllowed("1.1.1.1")).thenReturn(true);

        MvcResult mvcResult1 = this.mockMvc.perform(get(COUNTRY_FILTER_ALLOWED_ENDPOINT)
                .header("X-Real-IP", "1.1.1.1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(mvcResult1.getResponse().getContentAsString()).isEqualTo(
                jsonCountryFilterResponse.write(new CountryFilterResponse(true)).getJson()
        );

        when(mockCountryFilterService.isIPAllowed("1.1.1.1")).thenReturn(false);

        MvcResult mvcResult2 = this.mockMvc.perform(get(COUNTRY_FILTER_ALLOWED_ENDPOINT)
                .header("X-Real-IP", "1.1.1.1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(mvcResult2.getResponse().getContentAsString()).isEqualTo(
                jsonCountryFilterResponse.write(new CountryFilterResponse(false)).getJson()
        );

    }

}
