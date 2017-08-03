package io.modum.tokenapp.rates;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages={"io.modum.tokenapp.rates"})
@SpringBootApplication
public class RatesApplication implements CommandLineRunner {



    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RatesApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("fetching rates!!");
    }
}
