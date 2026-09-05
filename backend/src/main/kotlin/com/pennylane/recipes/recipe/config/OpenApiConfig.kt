package com.pennylane.recipes.recipe.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** OpenAPI document metadata; endpoints and schemas are documented via annotations on the web adapter. */
@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Recipe Finder API")
                    .version("0.0.1-SNAPSHOT")
                    .description("Find recipes you can cook with the ingredients you have."),
            )
}
