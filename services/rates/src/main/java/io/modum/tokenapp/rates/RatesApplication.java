package io.modum.tokenapp.rates;

import io.modum.tokenapp.rates.bean.Options;
import io.modum.tokenapp.rates.dao.ExchangeRateRepository;
import io.modum.tokenapp.rates.service.ExchangeRate;
import org.kohsuke.args4j.*;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

    @Autowired
    private Options options;

    @Argument
    private List<String> arguments = new ArrayList<>();

    @Autowired
    private ExchangeRateRepository repository;

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
        if (args.length == 0) {
            LOG.warn("No arguments provided. Defaulting to 30 second interval.");
            options.setRate(30);
        } else {
            CmdLineParser parser = new CmdLineParser(options);
            options.setRate(0);
            try {
                parser.parseArgument(args);
            } catch (CmdLineException e) {
                System.err.println(e.getMessage());
                System.err.println("java RatesApplication [options...] arguments...");
                parser.printUsage(System.err);
                System.err.println();
                System.err.println("  Example: java RatesApplication" + parser
                    .printExample(OptionHandlerFilter.ALL));
                return;
            }
        }
        if(options.getRate() > 0) {
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        exchangeRate.fetchRates();
                    } catch (IOException e) {
                        LOG.error("cannot fetch", e);
                    }
                }
            }, options.getRate() * 1000);
        }
        if(options.getExportFile() != null) {
            File file = new File(options.getExportFile());
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for(io.modum.tokenapp.rates.model.ExchangeRate p:repository.findAllByOrderByCreationDate()) {
                printWriter.print(p.getCreationDate().getTime());
                printWriter.print(",");
                printWriter.print(p.getBlockNrBtc());
                printWriter.print(",");
                printWriter.print(p.getBlockNrEth());
                printWriter.print(",");
                printWriter.print(p.getRateBtc());
                printWriter.print(",");
                printWriter.print(p.getRateBtcBitfinex());
                printWriter.print(",");
                printWriter.print(p.getRateIotaBitfinex());
                printWriter.print(",");
                printWriter.print(p.getRateEth());
                printWriter.print(",");
                printWriter.print(p.getRateEthBitfinex());
            }
            fileWriter.close();
        }
    }
}
