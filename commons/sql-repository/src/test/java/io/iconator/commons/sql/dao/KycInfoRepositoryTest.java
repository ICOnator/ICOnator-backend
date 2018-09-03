package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KycInfo;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class KycInfoRepositoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(KycInfoRepositoryTest.class);

    @Autowired
    private KycInfoRepository kycInfoRepository;

    @Test
    public void testSave() {
        KycInfo kycInfo = new KycInfo(1L,
                false,
                0,
                false,
                "www.kycuri.com");
        kycInfoRepository.save(kycInfo);
    }

    @Test
    public void testFindOptionalByInvestorId() {
        KycInfo kycInfo = createKycInfo(1, false);
        kycInfoRepository.save(kycInfo);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByInvestorId(1);
        assertTrue(oKycInfo.isPresent());
        assertEquals(1, (long)oKycInfo.get().getInvestorId());
    }

    @Test
    public void testFindOptionalByKycUuid() {
        KycInfo kycInfo = createKycInfo(1,false);
        KycInfo savedKycInfo = kycInfoRepository.save(kycInfo);

        Optional<KycInfo> oKycInfo = kycInfoRepository.findOptionalByKycUuid(savedKycInfo.getKycUuid());
        assertTrue(oKycInfo.isPresent());
        assertEquals(1, (long)oKycInfo.get().getInvestorId());
        assertEquals(savedKycInfo.getKycUuid(), oKycInfo.get().getKycUuid());
    }

    @Test
    public void testFindAllByIsKycComplete() {
        KycInfo kycInfo1 = createKycInfo(1, false);
        KycInfo kycInfo2 = createKycInfo(2, true);
        KycInfo kycInfo3 = createKycInfo(3, true);
        kycInfoRepository.save(kycInfo1);
        kycInfoRepository.save(kycInfo2);
        kycInfoRepository.save(kycInfo3);

        List<KycInfo> kycInfoListTrue = kycInfoRepository.findAllByIsKycComplete(true);
        assertEquals(2, kycInfoListTrue.size());
        assertThat(kycInfoListTrue, contains(
                hasProperty("investorId", is(2L)),
                hasProperty("investorId", is(3L))
        ));

        List<KycInfo> kycInfoListFalse = kycInfoRepository.findAllByIsKycComplete(false);
        assertEquals(1, kycInfoListFalse.size());
        assertThat(kycInfoListFalse, contains(
                hasProperty("investorId", is(1L))
        ));
    }

    private KycInfo createKycInfo(long i, boolean isKycComplete) {
        return new KycInfo(i,
                        false,
                        0,
                        isKycComplete,
                        "www.kycuri" + i + ".com");
    }


}
