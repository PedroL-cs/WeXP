package com.wexp.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wexpOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WeXP API")
                        .version("v1")
                        .description("API da aplicação WeXP."))
                .components(new Components()
                        .addSchemas("ApiErrorResponse", new Schema<>()
                                .type("object")
                                .addProperty("code", new Schema<Integer>().type("integer"))
                                .addProperty("status", new Schema<Integer>().type("integer"))
                                .addProperty("message", new Schema<String>().type("string"))
                                .addProperty("timestamp", new Schema<String>().type("string").format("date-time"))
                                .addProperty("path", new Schema<String>().type("string"))
                                .addProperty("errors", new ArraySchema()
                                        .items(new Schema<>().$ref("#/components/schemas/ApiFieldError"))))
                        .addSchemas("ApiFieldError", new Schema<>()
                                .type("object")
                                .addProperty("field", new Schema<String>().type("string"))
                                .addProperty("message", new Schema<String>().type("string")))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer pageableParametersCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() == null) {
                return operation;
            }

            operation.getParameters().stream()
                    .filter(parameter -> "query".equals(parameter.getIn()))
                    .forEach(parameter -> {
                        switch (parameter.getName()) {
                            case "page" -> parameter
                                    .description("Índice da página, começando em zero")
                                    .schema(new IntegerSchema().minimum(BigDecimal.ZERO).example(0));
                            case "size" -> parameter
                                    .description("Quantidade de itens por página")
                                    .schema(new IntegerSchema().minimum(BigDecimal.ONE).example(20));
                            case "sort" -> parameter
                                    .description("Ordenação no formato campo,asc ou campo,desc. Repita o parâmetro para múltiplas ordenações.")
                                    .schema(new StringSchema().example("name,asc"));
                            default -> {
                            }
                        }
                    });
            return operation;
        };
    }
}
