package io.iconator.core;

import io.iconator.core.config.MessageBrokerTestConfig;
import io.iconator.core.config.TestConfig;
import io.iconator.commons.test.utils.BaseMessageBrokerServiceTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                CoreApplication.class,
                TestConfig.class,
                MessageBrokerTestConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
// ;mv_store=false needed for correct isolation level:
// http://h2-database.66688.n3.nabble.com/Am-I-bananas-or-does-serializable-isolation-not-work-as-it-should-tp4030767p4030768.html
public abstract class BaseApplicationTest extends BaseMessageBrokerServiceTest {
}
