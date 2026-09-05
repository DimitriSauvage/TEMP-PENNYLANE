package com.pennylane.recipes.recipe.adapter.inbound.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.pennylane.recipes.recipe.adapter.outbound.persistence.AbstractPostgresIT
import com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding.RecipeSeeder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Full-stack web-layer tests: MockMvc over the real application context, real
 * Postgres catalog seeded from the shared search fixture, zero mocks.
 */
@AutoConfigureMockMvc
@SpringBootTest(
    properties = [
        "app.seed.url=classpath:seeding/search-fixture.json",
    ],
)
class RecipeApiIT(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val seeder: RecipeSeeder,
) : AbstractPostgresIT() {
    @BeforeEach
    fun reseedSharedCatalog() {
        // All ITs share one Postgres database and this context is cached across classes,
        // so another IT may have re-seeded the catalog since this context started. Seeding
        // is idempotent (TRUNCATE + insert), so re-running it keeps assertions order-independent.
        seeder.seed()
    }

    @Test
    fun `search request flows through sql and returns the ranked result as json`() {
        val body =
            mockMvc
                .get("/api/recipes/search") {
                    param("ingredients", "flour,eggs")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.page") { value(0) }
                    jsonPath("$.size") { value(20) }
                    jsonPath("$.totalElements") { value(9) }
                    jsonPath("$.items.length()") { value(9) }
                }.andReturn()
                .response.contentAsString

        val items = objectMapper.readTree(body).path("items")
        assertThat(items.map { it.path("name").asText() }).containsExactly(
            "Salted Eggs",
            "Instant Snack",
            "Simple Omelette",
            "Basic Bread",
            "Tie Recipe B",
            "Flourless Chocolate Cake",
            "Tie Recipe A",
            "Perfect Pancakes",
            "Suggestion Bucket",
        )
        val first = items.first()
        assertThat(first.path("id").asText()).isEqualTo("8")
        assertThat(first.path("matchedIngredients").asInt()).isEqualTo(2)
        assertThat(first.path("missingIngredients").isEmpty).isTrue()
        assertThat(first.path("readyToCook").asBoolean()).isTrue()
        val omelette = items[2]
        assertThat(omelette.path("totalIngredients").asInt()).isEqualTo(3)
        assertThat(omelette.path("matchedIngredients").asInt()).isEqualTo(2)
        assertThat(omelette.path("missingIngredients").first().asText()).isEqualTo("butter")
        assertThat(omelette.path("readyToCook").asBoolean()).isFalse()
    }

    @Test
    fun `readyToCookOnly filters over the real catalog`() {
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                param("readyToCookOnly", "true")
            }.andExpect {
                status { isOk() }
                jsonPath("$.totalElements") { value(2) }
                jsonPath("$.items.length()") { value(2) }
                jsonPath("$.items[0].readyToCook") { value(true) }
                jsonPath("$.items[0].missingIngredients.length()") { value(0) }
                jsonPath("$.items[1].readyToCook") { value(true) }
            }
    }

    @Test
    fun `empty or blank ingredient list yields 422 envelope with the empty list code`() {
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "")
            }.andExpect {
                status { isUnprocessableEntity() }
                jsonPath("$.status") { value(422) }
                jsonPath("$.code") { value("EMPTY_INGREDIENT_LIST") }
                jsonPath("$.path") { value("/api/recipes/search") }
                jsonPath("$.message") { isNotEmpty() }
            }
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", " , ")
            }.andExpect {
                status { isUnprocessableEntity() }
                jsonPath("$.code") { value("EMPTY_INGREDIENT_LIST") }
            }
    }

    @Test
    fun `unknown recipe id yields 404 envelope with the not found code`() {
        mockMvc
            .get("/api/recipes/999999")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
                jsonPath("$.code") { value("RECIPE_NOT_FOUND") }
                jsonPath("$.path") { value("/api/recipes/999999") }
                jsonPath("$.message") { isNotEmpty() }
            }
    }

    @Test
    fun `malformed params yield 400 envelopes`() {
        mockMvc
            .get("/api/recipes/abc")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_PARAMETER") }
                jsonPath("$.path") { value("/api/recipes/abc") }
            }
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                param("size", "big")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_PARAMETER") }
            }
        mockMvc
            .get("/api/recipes/search")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("MISSING_PARAMETER") }
            }
    }

    @Test
    fun `suggest endpoint matches prefix and caps at 10 suggestions`() {
        mockMvc
            .get("/api/ingredients/suggest") {
                param("q", "Chi")
            }.andExpect {
                status { isOk() }
                jsonPath("$.names.length()") { value(10) }
                jsonPath("$.names[0]") { value("chicken") }
                jsonPath("$.names[9]") { value("chili sauce") }
            }
    }

    @Test
    fun `suggest endpoint returns the exact alphabetical matches for a narrower prefix`() {
        mockMvc
            .get("/api/ingredients/suggest") {
                param("q", "chickpea")
            }.andExpect {
                status { isOk() }
                jsonPath("$.names.length()") { value(3) }
                jsonPath("$.names[0]") { value("chickpea") }
                jsonPath("$.names[1]") { value("chickpea flour") }
                jsonPath("$.names[2]") { value("chickpea pasta") }
            }
    }

    @Test
    fun `cors allows the frontend dev origin on the api and rejects others`() {
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                header(HttpHeaders.ORIGIN, "http://localhost:3000")
            }.andExpect {
                status { isOk() }
                header { string("Access-Control-Allow-Origin", "http://localhost:3000") }
            }
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                header(HttpHeaders.ORIGIN, "http://evil.example")
            }.andExpect {
                status { isForbidden() }
                header { doesNotExist("Access-Control-Allow-Origin") }
            }
    }
}
