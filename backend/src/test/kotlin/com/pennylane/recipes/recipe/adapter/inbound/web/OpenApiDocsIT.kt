package com.pennylane.recipes.recipe.adapter.inbound.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.pennylane.recipes.recipe.adapter.outbound.persistence.AbstractPostgresIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Proves the OpenAPI contract is exhaustive: every path, every parameter and
 * every response (success and error) is documented, and the shared error
 * envelope schema exists. The docs are static, so no seeding is needed.
 * (No own @SpringBootTest here: inheriting the base class's declaration keeps
 * the classpath seed fixture — an attribute-less re-declaration would shadow
 * it and fall back to the S3 URL.)
 */
@AutoConfigureMockMvc
class OpenApiDocsIT(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) : AbstractPostgresIT() {
    @Test
    fun `api-docs documents every path with tags, summary, description and every parameter`() {
        val docs = apiDocs()

        val paths = docs.path("paths")
        assertThat(paths.fieldNames().asSequence().toList())
            .containsExactlyInAnyOrder("/api/recipes/search", "/api/recipes/{id}", "/api/ingredients/suggest")

        val search = paths.path("/api/recipes/search").path("get")
        val detail = paths.path("/api/recipes/{id}").path("get")
        val suggest = paths.path("/api/ingredients/suggest").path("get")

        listOf(search, detail, suggest).forEach { op ->
            assertThat(op.path("tags").map(JsonNode::asText)).isNotEmpty()
            assertThat(op.path("summary").asText()).isNotBlank()
            assertThat(op.path("description").asText()).isNotBlank()
        }

        val searchParams = parameters(search)
        assertThat(searchParams.keys).containsExactlyInAnyOrder("ingredients", "readyToCookOnly", "page", "size")
        assertThat(searchParams.getValue("ingredients").path("required").asBoolean()).isTrue()
        assertThat(searchParams.getValue("readyToCookOnly").path("required").asBoolean()).isFalse()
        assertThat(searchParams.getValue("page").path("required").asBoolean()).isFalse()
        assertThat(searchParams.getValue("size").path("required").asBoolean()).isFalse()
        assertThat(searchParams.values.map { it.path("description").asText() }).allMatch { it.isNotBlank() }
        val pageSchema = searchParams.getValue("page").path("schema")
        assertThat(pageSchema.path("minimum").asInt(-1)).isZero()
        assertThat(pageSchema.path("maximum").asInt(-1)).isEqualTo(10_000)
        val sizeSchema = searchParams.getValue("size").path("schema")
        assertThat(sizeSchema.path("minimum").asInt(-1)).isEqualTo(1)
        assertThat(sizeSchema.path("maximum").asInt(-1)).isEqualTo(100)

        val detailParams = parameters(detail)
        assertThat(detailParams.keys).containsExactly("id")
        assertThat(detailParams.getValue("id").path("required").asBoolean()).isTrue()
        assertThat(detailParams.getValue("id").path("description").asText()).isNotBlank()

        val suggestParams = parameters(suggest)
        assertThat(suggestParams.keys).containsExactly("q")
        assertThat(suggestParams.getValue("q").path("required").asBoolean()).isTrue()
        assertThat(suggestParams.getValue("q").path("description").asText()).isNotBlank()
    }

    @Test
    fun `api-docs documents success and error responses plus the error envelope schema`() {
        val docs = apiDocs()

        val search = docs.path("paths").path("/api/recipes/search").path("get")
        val detail = docs.path("paths").path("/api/recipes/{id}").path("get")
        val suggest = docs.path("paths").path("/api/ingredients/suggest").path("get")

        assertThat(responseCodes(search)).containsExactlyInAnyOrder("200", "400", "422", "500")
        assertThat(responseCodes(detail)).containsExactlyInAnyOrder("200", "400", "404", "500")
        assertThat(responseCodes(suggest)).containsExactlyInAnyOrder("200", "400", "500")

        assertThat(payloadRef(search, "200")).isEqualTo("#/components/schemas/RecipeSearchResponseDto")
        assertThat(payloadRef(detail, "200")).isEqualTo("#/components/schemas/RecipeDetailResponseDto")
        assertThat(payloadRef(suggest, "200")).isEqualTo("#/components/schemas/IngredientSuggestionsResponseDto")

        listOf(search, detail, suggest).forEach { op ->
            responseCodes(op).filter { it != "200" }.forEach { code ->
                assertThat(payloadRef(op, code)).isEqualTo("#/components/schemas/ErrorResponse")
            }
        }

        val schemas = docs.path("components").path("schemas")
        assertThat(schemas.fieldNames().asSequence().toList()).contains(
            "ErrorResponse",
            "RecipeSearchResponseDto",
            "RecipeSearchItemDto",
            "RecipeDetailResponseDto",
            "IngredientSuggestionsResponseDto",
        )
    }

    private fun apiDocs(): JsonNode =
        mockMvc
            .get("/v3/api-docs")
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .let { objectMapper.readTree(it.contentAsString) }

    private fun parameters(operation: JsonNode): Map<String, JsonNode> =
        operation.path("parameters").associateBy { it.path("name").asText() }

    private fun responseCodes(operation: JsonNode): Set<String> =
        operation
            .path("responses")
            .fieldNames()
            .asSequence()
            .toSet()

    private fun payloadRef(
        operation: JsonNode,
        code: String,
    ): String =
        operation
            .path("responses")
            .path(code)
            .path("content")
            .path("application/json")
            .path("schema")
            .path("\$ref")
            .asText()
}
