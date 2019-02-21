package io.iconator.rates.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Programmatically sets the configuration for cross-origin resource sharing of the rates application.
 * The origin URLs for which to allow CORS must be specified in the application property
 * corresponding to {@link RatesCorsConfiguration#corsUrls}.
 */
@Configuration
public class RatesCorsConfiguration implements WebMvcConfigurer {

    @Value("${io.iconator.services.rates.cors.urls}")
    private String[] corsUrls;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins(this.corsUrls);
    }

}
