package io.iconator.kyc.service;

import io.iconator.commons.model.db.KycInfo;
import io.iconator.commons.sql.dao.KycInfoRepository;
import io.iconator.kyc.config.BaseKycTestConfig;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
import io.iconator.kyc.service.exception.KycInfoNotSavedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseKycTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:kyc.application.properties", "classpath:application-test.properties"})
public class KycInfoServiceTest {

    private static final String KYC_LINK = "http://www.kyctestlink.com/investor/12345678";

    @Autowired
    private KycInfoService kycInfoService;

    @Autowired
    private KycInfoRepository kycInfoRepository;

    private URI kycUri;

    @Before
    public void setUp() throws Exception {
        kycUri = new URI(KYC_LINK);
    }

    @Test
    public void testSaveKycInfo() throws URISyntaxException {
        try {
            kycInfoService.saveKycInfo(1, kycUri);
        } catch(KycInfoNotSavedException e) {
            fail(e.getMessage());
        }

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        if(oKycInfo.isPresent()) {
            assertKycInfo(oKycInfo.get(), 1, false, 0, false, kycUri);
        } else {
            fail("KycInfo should be present but is not.");
        }


        URI newKycUri = new URI(KYC_LINK + "doNotSaveThis");
        try {
            kycInfoService.saveKycInfo(1, newKycUri);
        } catch(KycInfoNotSavedException e) {
            fail(e.getMessage());
        }

        oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        if(oKycInfo.isPresent()) {
            assertKycInfo(oKycInfo.get(), 1, false, 0, false, kycUri);
        } else {
            fail("KycInfo should be present but is not.");
        }
    }

    @Test
    public void testSetKycComplete() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(2, kycUri);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(2);
        assertKycInfo(oKycInfo.get(), 2, false, 0, false, kycUri);

        try {
            kycInfoService.setKycComplete(2, true);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }
        oKycInfo = kycInfoRepository.findOptionalByInvestorId(2);
        assertKycInfo(oKycInfo.get(), 2, false, 0, true, kycUri);
    }

    @Test
    public void testSetKycCompleteByUuid() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(50, kycUri);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(50);
        assertKycInfo(oKycInfo.get(), 50, false, 0, false, kycUri);

        try {
            kycInfoService.setKycCompleteByUuid(oKycInfo.get().getKycUuid(), true);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        oKycInfo = kycInfoRepository.findOptionalByInvestorId(50);
        assertKycInfo(oKycInfo.get(), 50, false, 0, true, kycUri);
    }

    @Test
    public void testSetMultipleKycCompleteAndFindAll() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(1, kycUri);
        kycInfoService.saveKycInfo(2, kycUri);
        kycInfoService.saveKycInfo(3, kycUri);

        try {
            kycInfoService.setKycComplete(1, true);
            kycInfoService.setKycComplete(3, true);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        List<KycInfo> kycList = kycInfoRepository.findAll();
        assertEquals(3, kycList.size());

        List<KycInfo> kycCompleteList = kycInfoRepository.findAllByIsKycComplete(true);
        assertEquals(2, kycCompleteList.size());
    }

    @Test
    public void testSetKycStartEmailSent() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(1, kycUri);

        try {
            kycInfoService.setKycStartEmailSent(1);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        assertKycInfo(oKycInfo.get(), 1, true, 0, false, kycUri);
    }

    @Test
    public void testIncreaseNumberOfRemindersSent() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(1, kycUri);

        try {
            kycInfoService.increaseNumberOfRemindersSent(1);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        assertKycInfo(oKycInfo.get(), 1, false, 1, false, kycUri);

        try {
            kycInfoService.increaseNumberOfRemindersSent(1);
            kycInfoService.increaseNumberOfRemindersSent(1);
        } catch (InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        assertKycInfo(oKycInfo.get(), 1, false, 3, false, kycUri);
    }

    @Test
    public void testGetKycInfoByInvestorId() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(2000, kycUri);

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByInvestorId(2000);
            assertKycInfo(kycInfo, 2000, false, 0, false, kycUri);
        } catch (InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        try {
            kycInfoService.getKycInfoByInvestorId(1);
            fail("InvestorNotFoundException should have been thrown but wasn't.");
        } catch(InvestorNotFoundException expectedException) {

        }
    }

    @Test
    public void testGetKycInfoByKycUuid() throws KycInfoNotSavedException {
        kycInfoService.saveKycInfo(1, kycUri);

        UUID uuid = kycInfoRepository.findOptionalByInvestorId(1).get().getKycUuid();

        try {
            KycInfo kycInfo = kycInfoService.getKycInfoByKycUuid(uuid);
            assertKycInfo(kycInfo, 1, false, 0, false, kycUri);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetKycUri() throws KycInfoNotSavedException, URISyntaxException {
        URI newKycUri = new URI("http://www.newtestlink.com/test");
        kycInfoService.saveKycInfo(1, kycUri);

        try {
            kycInfoService.setKycUri(1, newKycUri.toASCIIString());
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        assertKycInfo(oKycInfo.get(), 1, false, 0, false, newKycUri);
    }

    private void assertKycInfo(KycInfo kycInfo,
                               long investorId,
                               boolean isKycStartEmailSent,
                               int noOfRemindersSent,
                               boolean isKycComplete,
                               URI kycUri) {
        assertEquals(investorId, kycInfo.getInvestorId());
        assertEquals(isKycStartEmailSent, kycInfo.isStartKycEmailSent());
        assertEquals(noOfRemindersSent, kycInfo.getNoOfRemindersSent());
        assertEquals(isKycComplete, kycInfo.isKycComplete());
        assertEquals(kycUri.toASCIIString(), kycInfo.getKycUri());
    }


}
