package io.modum.tokenapp.rates;

import io.modum.tokenapp.rates.service.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;

@ComponentScan(basePackages={"io.modum.tokenapp.rates"})
@SpringBootApplication
public class RatesApplication implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(RatesApplication.class);

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private ExchangeRate exchangeRate;

    @Bean
    ThreadPoolTaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RatesApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("fetching rates!!");
        if(args.length > 0) {
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        exchangeRate.fetchRates();
                    } catch (IOException e) {
                        LOG.error("cannot fetch", e);
                    }
                }
            }, 60 * 1000);
        }
    }
}
