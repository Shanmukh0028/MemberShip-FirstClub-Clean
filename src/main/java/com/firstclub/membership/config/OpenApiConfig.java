package com.firstclub.membership.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI membershipOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FirstClub Membership API")
                        .description("""
                                Backend system for the FirstClub Membership Program.
                                
                                **Design highlights:**
                                - Polymorphic benefit configs (Jackson `@JsonTypeInfo`) for type-safe service contracts
                                - Strategy Pattern for extensible tier evaluation
                                - JPA Optimistic Locking (`@Version`) for concurrent subscription modifications
                                - Data minimization on the internal benefits API via `?type=` filter
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FirstClub Engineering")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")));
    }
}
