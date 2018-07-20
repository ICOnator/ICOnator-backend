package io.iconator.kyc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.service.InvestorService;
import io.iconator.kyc.service.KycInfoService;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.Date;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class KycControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(KycControllerTest.class);

    private static final long INVESTOR_ID = 1;
    private static final String KYC_INVESTOR_START = "/kyc/" + INVESTOR_ID + "/start";
    private static final String KYC_INVESTOR_COMPLETE = "/kyc/" + INVESTOR_ID + "/complete";
    private static final String KYC_INVESTOR_STATUS = "/kyc/" + INVESTOR_ID + "/status";
    private static final String KYC_LINK = "http://www.kyctestlink.com/investor/12345678";
    private static final String INVESTOR_NOT_FOUND = "Investor not found in database.";

    private ObjectMapper mapper;

    @Mock
    private ICOnatorMessageService mockMessageService;

    @Mock
    private KycInfoService mockKycInfoService;

    @Mock
    private InvestorService mockInvestorService;

    @Mock
    private AmqpMessageFactory mockMessageFactory;

    @InjectMocks
    private KycController kycController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(kycController).build();
    }

    @Test
    public void testStartKycWithExistingInvestor() throws Exception {
        Investor investor = new Investor(new Date(), "test@test.com", "1234");
        KycStartEmailMessage message = new KycStartEmailMessage(build(investor), KYC_LINK);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());
        when(mockInvestorService.getInvestorByInvestorId(INVESTOR_ID)).thenReturn(investor);
        when(mockMessageFactory.makeKycStartEmailMessage(eq(investor), eq(new URI(KYC_LINK)))).thenReturn(message);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KYC_LINK))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(mockKycInfoService).saveKycInfo(INVESTOR_ID, new URI(KYC_LINK));
        verify(mockMessageService).send(eq(message));

        assertThat(result.getResponse().getContentAsString())
                .isEqualTo("Started KYC process for investor " + INVESTOR_ID);
    }

    @Test
    public void testStartKycWithNonexistentInvestor() throws Exception {
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());
        when(mockInvestorService.getInvestorByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KYC_LINK))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        verify(mockMessageService, never()).send(any(KycStartEmailMessage.class));
        assertThat(result.getResponse().getContentAsString()).isEqualTo(INVESTOR_NOT_FOUND);
    }

    @Test
    public void testStartKycWithMalformedUri() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content("this is not a correct url"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Illegal character in path");
    }

    @Test
    public void testStartAlreadyStartedKyc() throws Exception {
        // KYC not completed, Start Email not yet sent
        KycInfo testKycInfo1 = createKycInfo(false);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo1);

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KYC_LINK))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result1.getResponse().getContentAsString()).contains("started but start email not yet sent.");


        // KYC not completed, Start Email sent
        Investor investor = new Investor(new Date(), "test@test.com", "1234");
        KycInfo testKycInfo2 = createKycInfo(false).setStartKycEmailSent(true);
        KycReminderEmailMessage message = new KycReminderEmailMessage(build(investor), KYC_LINK);

        when(mockInvestorService.getInvestorByInvestorId(INVESTOR_ID)).thenReturn(investor);
        when(mockMessageFactory.makeKycReminderEmailMessage(eq(investor), eq(new URI(KYC_LINK)))).thenReturn(message);
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo2);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KYC_LINK))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(mockMessageService).send(eq(message));
        assertThat(result2.getResponse().getContentAsString()).contains("Sending Reminder.");


        // already completed KYC
        KycInfo testKycInfo3 = createKycInfo(true);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo3);

        MvcResult result3 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KYC_LINK))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result3.getResponse().getContentAsString()).contains("already complete.");
    }

    @Test
    public void testSetExistingInvestorCompleteWhenKycIncomplete() throws Exception {
        KycInfo testKycInfo = createKycInfo(false);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_COMPLETE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(mockKycInfoService).setKycComplete(testKycInfo.getInvestorId(), true);
    }

    @Test
    public void testSetExistingInvestorCompleteWhenKycAlreadyComplete() throws Exception {
        KycInfo testKycInfo = createKycInfo(true);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_COMPLETE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        verify(mockKycInfoService, never()).setKycComplete(testKycInfo.getInvestorId(), true);
    }

    @Test
    public void testSetNonexistentInvestorComplete() throws Exception {
        KycInfo testKycInfo = createKycInfo(false);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_COMPLETE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        verify(mockKycInfoService, never()).setKycComplete(testKycInfo.getInvestorId(), true);
        assertThat(result.getResponse().getContentAsString()).isEqualTo(INVESTOR_NOT_FOUND);
    }

    @Test
    public void testGetKycStatus() throws Exception {
        KycInfo kycInfo = createKycInfo(true);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(kycInfo);

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get(KYC_INVESTOR_STATUS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        KycInfo actual = mapper.readValue(result1.getResponse().getContentAsString(), KycInfo.class);
        assertThat(kycInfo.equals(actual));


        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get(KYC_INVESTOR_STATUS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result2.getResponse().getContentAsString()).isEqualTo(INVESTOR_NOT_FOUND);
    }

    private KycInfo createKycInfo(boolean isKycComplete) {
        return new KycInfo(INVESTOR_ID, isKycComplete, KYC_LINK);
    }
}
