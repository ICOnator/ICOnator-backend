package io.iconator.kyc.service.idnow;

import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.kyc.config.BaseKycTestConfig;
import io.iconator.kyc.config.KycConfigHolder;
import io.iconator.kyc.service.KycInfoService;
import io.iconator.kyc.service.KycLinkCreatorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseKycTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:kyc.application.properties", "classpath:application-test.properties"})
public class IdNowKycLinkCreatorServiceTest {

    @Autowired
    private KycInfoService kycInfoService;

    @Autowired
    private KycLinkCreatorService linkCreatorService;

    @Autowired
    private KycConfigHolder kycConfigHolder;

    private String targetKycLink;

    @Before
    public void setUp() throws Exception {
        kycInfoService.saveKycInfo(1, null);

        targetKycLink = kycConfigHolder.getIdNowHost() + "/" + kycConfigHolder.getIdNowCompanyId() + "/userdata/" + kycInfoService.getKycInfoByInvestorId(1).getKycUuid();
    }

    @Test
    public void testGetKycLink() throws InvestorNotFoundException {
        String kycLink = linkCreatorService.getKycLink(1);

        assertEquals(targetKycLink, kycLink);
    }

    @Test(expected = InvestorNotFoundException.class)
    public void testGetKycLink_nonExistentInvestor() throws InvestorNotFoundException {
        linkCreatorService.getKycLink(2);
    }

}
