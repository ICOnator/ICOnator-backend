package io.iconator.monitor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan("io.iconator.commons.model.db")
public class MonitorApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(MonitorApplication.class).run(args);
    }

}
