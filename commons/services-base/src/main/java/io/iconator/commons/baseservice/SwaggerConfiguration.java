package io.iconator.commons.baseservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Profile("!prod")
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("ICOnator",
                "An easy, secure, configurable, and scalable open source ICO engine -- driven by the community",
                null,
                null,
                new Contact("ICOnator Project", "https://iconator.io", "info@iconator.io"),
                "Apache 2",
                "",
                new ArrayList<VendorExtension>());
    }
}