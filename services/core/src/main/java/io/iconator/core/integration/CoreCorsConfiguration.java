package io.iconator.core.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Programmatically sets the configuration for cross-origin resource sharing of the core application.
 * The origin URLs for which to allow CORS must be specified in the application property
 * corresponding to {@link CoreCorsConfiguration#corsUrls}.
 */
@Configuration
public class CoreCorsConfiguration implements WebMvcConfigurer {

    @Value("${io.iconator.services.core.cors.urls}")
    private String[] corsUrls;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins(this.corsUrls);
    }

}
