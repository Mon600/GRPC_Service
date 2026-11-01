package com.shastage.store.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Store API",
                version = "v1",
                description = "API магазина для работы с клиентами, товарами, изображениями и поставщиками"
        )
)
public class OpenApiConfig {
}
