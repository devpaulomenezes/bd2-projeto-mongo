package br.edu.ifpb.es.bd2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marketplace NoSQL API — BD2")
                        .description("API REST NoSQL com MongoDB e Spring Boot desenvolvida para a disciplina de Banco de Dados II (BD2) - IFPB")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipe BD2 - IFPB")
                                .url("https://github.com/devpaulomenezes/bd2-projeto-mongo"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
