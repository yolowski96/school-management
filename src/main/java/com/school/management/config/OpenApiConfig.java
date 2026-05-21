package com.school.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Management API")
                        .description("REST API for managing students, teachers and courses, with reporting.")
                        .version("v1")
                        .contact(new Contact().name("School Management"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
