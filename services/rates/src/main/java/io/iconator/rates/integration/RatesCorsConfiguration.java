package io.iconator.rates.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RatesCorsConfiguration implements WebMvcConfigurer {

    @Value("${io.iconator.services.rates.cors.urls}")
    private String[] corsUrls;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins(this.corsUrls);
    }

}
