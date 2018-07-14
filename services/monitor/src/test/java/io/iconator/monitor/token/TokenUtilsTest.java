package io.iconator.monitor.token;

import io.iconator.monitor.config.MonitorAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MonitorAppConfig.class})
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class TokenUtilsTest {

    @Autowired
    private MonitorAppConfig appConfig;

    @Test
    public void testConvertUsdToTomics() {
        BigDecimal usd = new BigDecimal("1");
        BigDecimal discount = new BigDecimal("0.25", new MathContext(6, RoundingMode.HALF_EVEN));

        BigDecimal tomics = TokenUtils.convertUsdToTomics(usd, appConfig.getUsdPerToken(), discount);
        BigDecimal expectedResult = new BigDecimal("40").multiply(TokenUnit.TOKEN.getTomicFactor())
                .divide(new BigDecimal("3"), new MathContext(34, RoundingMode.DOWN));

        assertEquals(0, tomics.compareTo(expectedResult));
    }

    @Test
    public void testConvertTomicsToUsd() {
        BigDecimal tomics = new BigDecimal("3.333").multiply(TokenUnit.TOKEN.getTomicFactor());
        BigDecimal discount = new BigDecimal("0.333333", new MathContext(6, RoundingMode.HALF_EVEN));

        BigDecimal usd = TokenUtils.convertTomicsToUsd(tomics, appConfig.getUsdPerToken(), discount);
        BigDecimal expectedResult = new BigDecimal("3.333")
                .multiply(BigDecimal.ONE.subtract(discount), MathContext.DECIMAL128)
                .multiply(appConfig.getUsdPerToken());
        assertEquals(0, usd.compareTo(expectedResult));
    }
}