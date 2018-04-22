package io.iconator.commons.db.services.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"io.iconator.commons.model.db"})
@EnableJpaRepositories("io.iconator.commons.sql.dao")
public class TestConfig {

}
