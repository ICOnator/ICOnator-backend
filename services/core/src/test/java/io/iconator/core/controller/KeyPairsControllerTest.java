package io.iconator.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.auth.ActuatorAuthSecurityConfig;
import io.iconator.commons.db.services.KeyPairsRepositoryService;
import io.iconator.commons.model.db.KeyPairs;
import io.iconator.core.controller.exceptions.CSVImportException;
import io.iconator.core.dto.KeyPairsImportResponse;
import io.iconator.core.service.CSVService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ActuatorAuthSecurityConfig.class, FilterChainProxy.class})
@TestPropertySource({"classpath:core.application.properties", "classpath:application-test.properties"})
public class KeyPairsControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(KeyPairsControllerTest.class);

    @Autowired
    FilterChainProxy springSecurityFilterChain;

    @Autowired
    ActuatorAuthSecurityConfig actuatorAuthSecurityConfig;

    @Mock
    private CSVService mockCsvService;

    @Mock
    private KeyPairsRepositoryService mockKeyPairsRepositoryService;

    @InjectMocks
    private KeyPairsController keyPairsController;

    private JacksonTester<KeyPairsImportResponse> jsonKeyPairsImportResponse;
    private JacksonTester<CSVImportException> jsonCSVImportException;

    private MockMvc mockMvc;

    private InputStream csvStream;

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders.standaloneSetup(keyPairsController).apply(springSecurity(springSecurityFilterChain)).build();
        this.csvStream = this.getClass().getResourceAsStream("/keypairs-test.csv");
    }

    @Test
    public void testImport_Successful() throws Exception {

        List<KeyPairs> keyPairsList = Arrays.asList(
                new KeyPairs("mgfRrsDH56YfTgw3pWJg7j4yEaUgxJxxim", "0x0eB5C5de600D088AB0260d068E9765022FD5173b"),
                new KeyPairs("ms8Ux2eEMFTq4HKXiCiPGCXzrV9dPeALx", "0x0eB5C5de600D088AB0260d068E9765022FD5173b"),
                new KeyPairs("mhCKesVtR6coeLRGXg3V8D7cQmgjXzaQj2", "0x3Bf54439C6056B564C14986b08e46637dD438372"),
                new KeyPairs("muBFsGpYiDLAcTbqZ4vcHsf9dnUTjBoVKg", "0x2D4A5a6FDf6f29ce236251974572ead525eC319D")
        );

        when(mockCsvService.fromCSV(any())).thenReturn(keyPairsList);
        when(mockKeyPairsRepositoryService.addKeyPairsIfNotPresent(any())).thenReturn(new Boolean(true));

        MvcResult mvcResult = this.mockMvc.perform(post("/keypairs/import")
                .with(httpBasic("user", "password"))
                .contentType(APPLICATION_OCTET_STREAM_VALUE)
                .content(IOUtils.toByteArray(csvStream)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(
                jsonKeyPairsImportResponse.write(new KeyPairsImportResponse(new Long(4), new Long(0))).getJson()
        );
    }

    @Test
    public void testImport_1_Failed() throws Exception {

        KeyPairs k1 = new KeyPairs("mgfRrsDH56YfTgw3pWJg7j4yEaUgxJxxim", "0x0eB5C5de600D088AB0260d068E9765022FD5173b");
        KeyPairs k2 = new KeyPairs("ms8Ux2eEMFTq4HKXiCiPGCXzrV9dPeALx", "0x0eB5C5de600D088AB0260d068E9765022FD5173b");
        KeyPairs k3 = new KeyPairs("mhCKesVtR6coeLRGXg3V8D7cQmgjXzaQj2", "0x3Bf54439C6056B564C14986b08e46637dD438372");
        KeyPairs k4 = new KeyPairs("muBFsGpYiDLAcTbqZ4vcHsf9dnUTjBoVKg", "0x2D4A5a6FDf6f29ce236251974572ead525eC319D");

        List<KeyPairs> keyPairsList = Arrays.asList(k1, k2, k3, k4);

        when(mockCsvService.fromCSV(any())).thenReturn(keyPairsList);
        when(mockKeyPairsRepositoryService.addKeyPairsIfNotPresent(k1)).thenReturn(new Boolean(false));
        when(mockKeyPairsRepositoryService.addKeyPairsIfNotPresent(k2)).thenReturn(new Boolean(true));
        when(mockKeyPairsRepositoryService.addKeyPairsIfNotPresent(k3)).thenReturn(new Boolean(true));
        when(mockKeyPairsRepositoryService.addKeyPairsIfNotPresent(k4)).thenReturn(new Boolean(true));

        MvcResult mvcResult = this.mockMvc.perform(post("/keypairs/import")
                .with(httpBasic("user", "password"))
                .contentType(APPLICATION_OCTET_STREAM_VALUE)
                .content(IOUtils.toByteArray(csvStream)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(
                jsonKeyPairsImportResponse.write(new KeyPairsImportResponse(new Long(3), new Long(1))).getJson()
        );
    }

    @Test
    public void testImport_Empty_CSV() throws Exception {

        List<KeyPairs> keyPairsList = Arrays.asList();

        when(mockCsvService.fromCSV(any())).thenReturn(keyPairsList);

        MvcResult mvcResult = this.mockMvc.perform(post("/keypairs/import")
                .with(httpBasic("user", "password"))
                .contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(
                jsonKeyPairsImportResponse.write(new KeyPairsImportResponse(new Long(0), new Long(0))).getJson()
        );
    }

    @Test
    public void testImport_Exception() throws Exception {

        when(mockCsvService.fromCSV(any())).thenThrow(new Exception());

        MvcResult mvcResult = this.mockMvc.perform(post("/keypairs/import")
                .with(httpBasic("user", "password"))
                .contentType(APPLICATION_OCTET_STREAM_VALUE)
                .content(IOUtils.toByteArray(csvStream)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertEquals(new CSVImportException().getMessage(), mvcResult.getResponse().getErrorMessage());
    }

    @Test
    public void testImport_Wrong_Credentials() throws Exception {

        when(mockCsvService.fromCSV(any())).thenThrow(new Exception());

        this.mockMvc.perform(post("/keypairs/import")
                .with(httpBasic("test", "password"))
                .contentType(APPLICATION_OCTET_STREAM_VALUE)
                .content(IOUtils.toByteArray(csvStream)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void testImport_No_Credentials() throws Exception {

        when(mockCsvService.fromCSV(any())).thenThrow(new Exception());

        this.mockMvc.perform(post("/keypairs/import")
                .contentType(APPLICATION_OCTET_STREAM_VALUE)
                .content(IOUtils.toByteArray(csvStream)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

}
