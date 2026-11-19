package com.carddemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for CardDemo API.
 * Configures API documentation with title, description, version, contact, and license.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CardDemo API")
                .description("RESTful API for credit card account and transaction management.\n\n" +
                    "Java 17 Spring Boot 3.x backend converted from COBOL system using AWS Transform " +
                    "reverse engineering documentation.\n\n" +
                    "**Source Programs Implemented:**\n" +
                    "- CBACT01C-04C: Account management, balance tracking, interest calculation\n" +
                    "- CBTRN01C-03C: Transaction posting, settlement, reporting\n" +
                    "- COPAUA0C: Real-time authorization decisions\n" +
                    "- COPAUS0C/1C/2C: Pending authorization review\n" +
                    "- CBPAUP0C: Authorization expiration management\n" +
                    "- COCRDUPC: Card issuance and reissue\n" +
                    "- COTRTUPC: Transaction type definitions")
                .version("1.0.0")
                .contact(new Contact()
                    .name("CardDemo Development Team")
                    .email("dev@carddemo.com")
                    .url("https://github.com/carddemo/carddemo-modernization"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://carddemo.com/license")));
    }
}
