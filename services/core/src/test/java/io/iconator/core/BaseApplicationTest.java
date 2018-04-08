package io.iconator.core;

import io.iconator.commons.test.utils.BaseMessageBrokerServiceTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource({"classpath:core.application.properties", "classpath:application-test.properties"})
public abstract class BaseApplicationTest extends BaseMessageBrokerServiceTest {
}
