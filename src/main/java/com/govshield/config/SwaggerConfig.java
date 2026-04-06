package com.govshield.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("GovShield API")
                .version("1.0.0")
                .description("Unified Beneficiary & Corruption Monitoring Platform for Indian Government")
                .contact(new Contact()
                    .name("GovShield Team")
                    .url("http://govshield.gov.in")));
    }
}
