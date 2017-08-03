package io.modum.tokenapp.rates;

import io.modum.tokenapp.rates.service.ExchangeRate;
import org.kohsuke.args4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ComponentScan(basePackages={"io.modum.tokenapp.rates"})
@SpringBootApplication
public class RatesApplication implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(RatesApplication.class);

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private ExchangeRate exchangeRate;

    @Option(name="-r",usage="rate to query APIs")
    private int rate;

    @Argument
    private List<String> arguments = new ArrayList<>();

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
        CmdLineParser parser = new CmdLineParser(this);
        rate = 0;
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java RatesApplication [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java RatesApplication"+parser.printExample(OptionHandlerFilter.ALL));
            return;
        }
        if(rate > 0) {
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        exchangeRate.fetchRates();
                    } catch (IOException e) {
                        LOG.error("cannot fetch", e);
                    }
                }
            }, rate * 1000);
        }
    }
}
