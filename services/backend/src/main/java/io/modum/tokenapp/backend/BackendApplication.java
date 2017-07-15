package io.modum.tokenapp.backend;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableZuulProxy
public class BackendApplication {

    public static void main(String[] args) {
        run(BackendApplication.class, args);
    }

}
