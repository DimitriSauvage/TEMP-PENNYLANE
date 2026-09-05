package com.pennylane.recipes.recipe.adapter.inbound.web.controller

import com.pennylane.recipes.recipe.adapter.outbound.persistence.AbstractPostgresIT
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Proves the CORS origin list is wired from `app.cors.allowed-origins`: a
 * context started with a custom list honors it (and drops the default).
 * Separate context from [RecipeApiIT] because Spring caches per property set.
 */
@AutoConfigureMockMvc
@SpringBootTest(
    properties = [
        "app.cors.allowed-origins=https://app.example,https://other.example",
        // classpath fixture so the boot-time seed of an empty catalog never hits the network
        "app.seed.url=classpath:seeding/search-fixture.json",
    ],
)
class CorsOriginsConfigIT(
    private val mockMvc: MockMvc,
) : AbstractPostgresIT() {
    @Test
    fun `custom allowed origins from configuration are honored`() {
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                header(HttpHeaders.ORIGIN, "https://app.example")
            }.andExpect {
                status { isOk() }
                header { string("Access-Control-Allow-Origin", "https://app.example") }
            }
    }

    @Test
    fun `the default dev origin is dropped when a custom list is set`() {
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                header(HttpHeaders.ORIGIN, "http://localhost:3000")
            }.andExpect {
                status { isForbidden() }
                header { doesNotExist("Access-Control-Allow-Origin") }
            }
    }
}
