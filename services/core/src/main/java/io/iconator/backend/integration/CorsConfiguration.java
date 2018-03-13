package io.iconator.backend.integration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Profile({"!prod"})
public class CorsConfiguration implements WebMvcConfigurer {
    @Value("${io.iconator.backend.frontendUrl}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins("GET", "OPTIONS").allowedOrigins(this.frontendUrl);
    }
}
