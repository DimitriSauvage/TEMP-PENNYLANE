package com.pennylane.recipes.recipe.adapter.inbound.web.controller

import com.github.michaelbull.result.Ok
import com.ninjasquad.springmockk.MockkBean
import com.pennylane.recipes.recipe.application.command.SuggestIngredientsQuery
import com.pennylane.recipes.recipe.application.model.IngredientSuggestions
import com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase
import com.pennylane.recipes.recipe.domain.model.IngredientName
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(IngredientController::class)
class IngredientControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var suggestIngredients: SuggestIngredientsUseCase

    @Test
    fun `suggest maps q param to the command and returns 200 with the names`() {
        every { suggestIngredients.execute(any()) } returns Ok(IngredientSuggestions(listOf("chicken", "chicken broth")))

        mockMvc
            .get("/api/ingredients/suggest") {
                param("q", "Chi")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.names.length()") { value(2) }
                jsonPath("$.names[0]") { value("chicken") }
                jsonPath("$.names[1]") { value("chicken broth") }
            }

        verify { suggestIngredients.execute(SuggestIngredientsQuery(prefix = IngredientName.of("Chi"))) }
    }

    @Test
    fun `suggest without required q param returns 400 error envelope`() {
        mockMvc
            .get("/api/ingredients/suggest")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value(400) }
                jsonPath("$.code") { value("MISSING_PARAMETER") }
                jsonPath("$.path") { value("/api/ingredients/suggest") }
            }
    }
}
