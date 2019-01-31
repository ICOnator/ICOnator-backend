package io.iconator.commons.recaptcha;

import io.iconator.commons.recaptcha.config.RecaptchaConfig;
import io.iconator.commons.recaptcha.config.RecaptchaConfigHolder;
import io.iconator.commons.recaptcha.exceptions.RecaptchaException;
import io.iconator.commons.recaptcha.model.RecaptchaResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RecaptchaConfig.class, RecaptchaConfigHolder.class, RecaptchaClientService.class})
@TestPropertySource("classpath:application-test.properties")
public class RecaptchaClientServiceTest {

    @MockBean
    private RestTemplate restTemplate;

    @SpyBean
    private RecaptchaConfigHolder recaptchaConfigHolder;

    @Autowired
    private RecaptchaClientService recaptchaClientService;

    @Test
    public void testVerify() {
        RecaptchaResponse recaptchaResponse = new RecaptchaResponse(true, new Date(), "hostname");
        ResponseEntity responseEntity = new ResponseEntity(recaptchaResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(RecaptchaResponse.class))).thenReturn(responseEntity);
        boolean result = recaptchaClientService.verify("1.2.3.4", "recaptcha-response-test");
        assertTrue(result);
    }

    @Test
    public void testVerify_Not_Enabled() {
        doReturn(false).when(recaptchaConfigHolder).isEnabled();
        boolean result = recaptchaClientService.verify("1.2.3.4", "recaptcha-response-test");
        assertTrue(result);
    }

    @Test
    public void testVerify_Null_Response() {
        boolean result = recaptchaClientService.verify("1.2.3.4", null);
        assertFalse(result);
    }

    @Test(expected = RecaptchaException.class)
    public void testVerify_Internal_Server_Error() {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(any(), eq(RecaptchaResponse.class))).thenReturn(responseEntity);
        boolean result = recaptchaClientService.verify("1.2.3.4", "recaptcha-response-test");
        assertFalse(result);
    }

    @Test
    public void testVerify_Invalid_Input_Response() {
        String[] errors = new String[]{RecaptchaClientService.INVALID_INPUT_RESPONSE};
        RecaptchaResponse recaptchaResponse = new RecaptchaResponse(false, new Date(), "hostname", errors);
        ResponseEntity responseEntity = new ResponseEntity(recaptchaResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(RecaptchaResponse.class))).thenReturn(responseEntity);
        boolean result = recaptchaClientService.verify("1.2.3.4", "recaptcha-response-test");
        assertFalse(result);
    }

    @Test
    public void testVerify_Missing_Input_Response() {
        String[] errors = new String[]{RecaptchaClientService.MISSING_INPUT_RESPONSE};
        RecaptchaResponse recaptchaResponse = new RecaptchaResponse(false, new Date(), "hostname", errors);
        ResponseEntity responseEntity = new ResponseEntity(recaptchaResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(), eq(RecaptchaResponse.class))).thenReturn(responseEntity);
        boolean result = recaptchaClientService.verify("1.2.3.4", "recaptcha-response-test");
        assertFalse(result);
    }

}
