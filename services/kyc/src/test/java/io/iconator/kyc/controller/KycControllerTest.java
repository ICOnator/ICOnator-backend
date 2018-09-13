package io.iconator.kyc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.KycInfo;
import io.iconator.kyc.dto.CompleteSingleKycResponseDTO;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.KycStartRequestDTO;
import io.iconator.kyc.dto.RemindSingleKycResponseDTO;
import io.iconator.kyc.dto.StartAllKycResponseDTO;
import io.iconator.kyc.dto.StartSingleKycResponseDTO;
import io.iconator.kyc.service.*;
import io.iconator.kyc.service.idnow.dto.IdNowIdentificationProcess;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_COMPLETED_REASON;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_STARTED_WITHOUT_EMAIL_REASON;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_STARTED_WITH_EMAIL_REASON;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_LINK_REASON;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_NOT_YET_STARTED_REASON;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.NONEXISTENT_INVESTOR_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class KycControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(KycControllerTest.class);

    private static final long INVESTOR_ID = 1;
    private static final String KYC_START_ALL = "/kyc/start";
    private static final String KYC_INVESTOR_START = "/kyc/" + INVESTOR_ID + "/start";
    private static final String KYC_INVESTOR_COMPLETE = "/kyc/" + INVESTOR_ID + "/complete";
    private static final String KYC_INVESTOR_STATUS = "/kyc/" + INVESTOR_ID + "/status";
    private static final String KYC_FETCH_ALL_IDENTIFICATIONS = "/kyc/fetchall";
    private static final String KYC_REMIND = "/kyc/" + INVESTOR_ID + "/remind";
    private static final String KYC_LINK = "http://www.kyctestlink.com/investor/12345678";

    @Mock
    private ICOnatorMessageService mockMessageService;

    @Mock
    private KycInfoService mockKycInfoService;

    @Mock
    private InvestorService mockInvestorService;

    @Mock
    private PaymentLogService mockPaymentLogService;

    @Mock
    private AmqpMessageFactory mockMessageFactory;

    @Mock
    private KycLinkCreatorService mockLinkCreatorService;

    @Mock
    private IdentificationService mockIdentificationService;

    @InjectMocks
    private KycController kycController;

    private JacksonTester<CompleteSingleKycResponseDTO> jsonCompleteSingleKycResponse;
    private JacksonTester<StartSingleKycResponseDTO> jsonStartSingleKycResponse;
    private JacksonTester<RemindSingleKycResponseDTO> jsonRemindSingleKycResponse;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private KycStartRequestDTO kycStartRequest;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        JacksonTester.initFields(this, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(kycController).build();
        kycStartRequest = new KycStartRequestDTO(KYC_LINK);
    }

    @Test
    public void testStartKycForAll() throws Exception {
        List<Investor> investorList = new ArrayList<>();
        List<Long> kycStartedList = new ArrayList<>();
        Investor investor1 = new Investor(new Date(), "investor1@test.com", "investor1token").setId(1);
        Investor investor2 = new Investor(new Date(), "investor2@test.com", "investor2token").setId(2);
        Investor investor3 = new Investor(new Date(), "investor3@test.com", "investor3token").setId(3);
        Investor investor4 = new Investor(new Date(), "investor4@test.com", "investor4token").setId(4);
        investorList.add(investor1);
        investorList.add(investor2);
        investorList.add(investor3);
        investorList.add(investor4);
        kycStartedList.add(2L);

        when(mockInvestorService.getAllInvestors()).thenReturn(investorList);
        when(mockKycInfoService.getAllInvestorIdWhereStartKycEmailSent()).thenReturn(kycStartedList);
        when(mockLinkCreatorService.getKycLink(anyLong())).thenReturn(KYC_LINK);
        when(mockPaymentLogService.hasInvestorInvested(1)).thenReturn(true);
        when(mockPaymentLogService.hasInvestorInvested(3)).thenReturn(false);
        when(mockPaymentLogService.hasInvestorInvested(4)).thenReturn(true);


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_START_ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        StartAllKycResponseDTO responseDTO =
                mapper.readValue(result.getResponse().getContentAsString(), StartAllKycResponseDTO.class);

        assertThat(responseDTO.getKycStartedList()).containsExactly(1L, 4L);
        assertThat(responseDTO.getErrorList()).isEmpty();
    }

    @Test
    public void testStartKycWithExistingInvestor() throws Exception {
        Investor investor = new Investor(new Date(), "test@test.com", "1234");
        KycStartEmailMessage message = new KycStartEmailMessage(build(investor), KYC_LINK);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());
        when(mockInvestorService.getInvestorByInvestorId(INVESTOR_ID)).thenReturn(investor);
        when(mockMessageFactory.makeKycStartEmailMessage(eq(investor), eq(new URI(KYC_LINK)))).thenReturn(message);

        // start with link
        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(kycStartRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(result1.getResponse().getContentAsString()).isEqualTo(
                jsonStartSingleKycResponse.write(new StartSingleKycResponseDTO(INVESTOR_ID, true)).getJson()
        );

        // start without providing link
        when(mockLinkCreatorService.getKycLink(INVESTOR_ID)).thenReturn(KYC_LINK);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertThat(result2.getResponse().getContentAsString()).isEqualTo(
                jsonStartSingleKycResponse.write(new StartSingleKycResponseDTO(INVESTOR_ID, true)).getJson()
        );
        verify(mockMessageService, times(2)).send(eq(message));
    }

    @Test
    public void testStartKycWithNonexistentInvestor() throws Exception {
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());
        when(mockInvestorService.getInvestorByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(kycStartRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        verify(mockMessageService, never()).send(any(KycStartEmailMessage.class));
        assertThat(result.getResponse().getErrorMessage()).isEqualTo(NONEXISTENT_INVESTOR_REASON);
    }

    @Test
    public void testStartKycWithMalformedUri() throws Exception {
        KycStartRequestDTO requestWithMalformedUri = new KycStartRequestDTO("this is not a url");
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestWithMalformedUri)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result.getResponse().getErrorMessage()).isEqualTo(KYC_LINK_REASON);
    }

    @Test
    public void testStartAlreadyStartedKyc() throws Exception {
        // KYC not completed, Start Email not yet sent
        KycInfo testKycInfo1 = createKycInfo(false, 0,false);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo1);

        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(kycStartRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result1.getResponse().getErrorMessage()).isEqualTo(KYC_ALREADY_STARTED_WITHOUT_EMAIL_REASON);


        // KYC not completed, Start Email sent
        Investor investor = new Investor(new Date(), "test@test.com", "1234");
        KycInfo testKycInfo2 = createKycInfo(true, 0,false);
        KycReminderEmailMessage message = new KycReminderEmailMessage(build(investor), KYC_LINK);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo2);

        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(kycStartRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result2.getResponse().getErrorMessage()).isEqualTo(KYC_ALREADY_STARTED_WITH_EMAIL_REASON);


        // already completed KYC
        KycInfo testKycInfo3 = createKycInfo(true, 0,true);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(testKycInfo3);

        MvcResult result3 = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_START)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(kycStartRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result3.getResponse().getErrorMessage()).isEqualTo(KYC_ALREADY_COMPLETED_REASON);
    }

    @Test
    public void testSetExistingInvestorCompleteWhenKycIncomplete() throws Exception {
        KycInfo testKycInfo = createKycInfo(true, 0,false);

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
        KycInfo testKycInfo = createKycInfo(true, 0,true);

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
        KycInfo testKycInfo = createKycInfo(true, 0, false);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_INVESTOR_COMPLETE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        verify(mockKycInfoService, never()).setKycComplete(testKycInfo.getInvestorId(), true);
        assertThat(result.getResponse().getErrorMessage()).isEqualTo(NONEXISTENT_INVESTOR_REASON);
    }

    @Test
    public void testGetKycStatus() throws Exception {
        KycInfo kycInfo = createKycInfo(true, 0,true);

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

        assertThat(result2.getResponse().getErrorMessage()).isEqualTo(NONEXISTENT_INVESTOR_REASON);
    }

    @Test
    public void testFetchAllKycIdentifications() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Identification id1 = new IdNowIdentificationProcess("SUCCESS", new Date(), uuid1.toString());
        Identification id2 = new IdNowIdentificationProcess("FAILURE", new Date(), uuid2.toString());
        List<Identification> idList = new ArrayList<>();
        idList.add(id1);
        idList.add(id2);

        when(mockIdentificationService.fetchIdentifications()).thenReturn(idList);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(KYC_FETCH_ALL_IDENTIFICATIONS))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(mockKycInfoService, times(1)).setKycCompleteByUuid(uuid1, true);
    }

    @Test
    public void testKycReminder() throws Exception {
        Investor investor = new Investor(new Date(), "test@test.com", "1234");
        KycReminderEmailMessage message = new KycReminderEmailMessage(build(investor), KYC_LINK);
        KycInfo kycInfo = createKycInfo(true, 0, false);

        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(kycInfo);
        when(mockInvestorService.getInvestorByInvestorId(INVESTOR_ID)).thenReturn(investor);
        when(mockMessageFactory.makeKycReminderEmailMessage(eq(investor), eq(new URI(KYC_LINK)))).thenReturn(message);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_REMIND))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(mockMessageService, times(1)).send(any(KycReminderEmailMessage.class));
        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                jsonRemindSingleKycResponse.write(new RemindSingleKycResponseDTO(INVESTOR_ID, true)).getJson()
        );
    }

    @Test
    public void testKycReminder_nonexistentInvestor() throws Exception {
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenThrow(new InvestorNotFoundException());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_REMIND))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result.getResponse().getErrorMessage()).isEqualTo(NONEXISTENT_INVESTOR_REASON);
    }

    @Test
    public void testKycReminder_kycNotStarted() throws Exception {
        KycInfo kycInfo = createKycInfo(false, 0, false);
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(kycInfo);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_REMIND))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result.getResponse().getErrorMessage()).isEqualTo(KYC_NOT_YET_STARTED_REASON);
    }

    @Test
    public void testKycReminder_kycComplete() throws Exception {
        KycInfo kycInfo = createKycInfo(true, 0, true);
        when(mockKycInfoService.getKycInfoByInvestorId(INVESTOR_ID)).thenReturn(kycInfo);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(KYC_REMIND))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        assertThat(result.getResponse().getErrorMessage()).isEqualTo(KYC_ALREADY_COMPLETED_REASON);
    }

    private KycInfo createKycInfo(boolean isStartKycEmailSent, int noOfRemindersSent, boolean isKycComplete) {
        return new KycInfo(INVESTOR_ID, isStartKycEmailSent, noOfRemindersSent, isKycComplete, KYC_LINK);
    }
}
