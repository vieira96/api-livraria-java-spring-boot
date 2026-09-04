package com.cursojava.libraryapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI libraryApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library API")
                        .version("v1")
                        .description("API para gerenciamento de autores e livros."));
    }
}
