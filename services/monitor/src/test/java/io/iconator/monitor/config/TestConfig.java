package io.iconator.monitor.config;

import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.BaseMonitor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class TestConfig {

    @Bean
    public BaseMonitor baseMonitor(SaleTierRepository saleTierRepository) {
        return new BaseMonitor(saleTierRepository);
    }
}

