package com.nikookinn.librarymanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .description("Library Management System REST API documentation")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("nikookinn")
                                .email("nikookinn@hotmail.com")));
    }
}
