package com.pennylane.recipes.recipe.adapter.inbound.web.controller

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.ninjasquad.springmockk.MockkBean
import com.pennylane.recipes.recipe.application.command.GetRecipeQuery
import com.pennylane.recipes.recipe.application.command.SearchRecipesQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.RecipeDetail
import com.pennylane.recipes.recipe.application.model.RecipeSearchItem
import com.pennylane.recipes.recipe.application.model.RecipeSearchResult
import com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase
import com.pennylane.recipes.recipe.application.port.inbound.SearchRecipesUseCase
import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.model.RecipeId
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(RecipeController::class)
class RecipeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var searchRecipes: SearchRecipesUseCase

    @MockkBean
    private lateinit var getRecipe: GetRecipeUseCase

    @Test
    fun `search maps ingredients to the command and returns 200 with the result dto`() {
        val result =
            RecipeSearchResult(
                page = 0,
                size = 20,
                totalElements = 2,
                items =
                    listOf(
                        RecipeSearchItem(
                            id = "7",
                            name = "Instant Snack",
                            imageUrl = null,
                            totalIngredients = 1,
                            matchedIngredients = 1,
                            missingIngredients = emptyList(),
                            readyToCook = true,
                        ),
                        RecipeSearchItem(
                            id = "1",
                            name = "Perfect Pancakes",
                            imageUrl = "http://img.example/pancakes.jpg",
                            totalIngredients = 5,
                            matchedIngredients = 2,
                            missingIngredients = listOf("milk"),
                            readyToCook = false,
                        ),
                    ),
            )
        every { searchRecipes.execute(any()) } returns Ok(result)

        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", " flour , eggs ")
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.page") { value(0) }
                jsonPath("$.size") { value(20) }
                jsonPath("$.totalElements") { value(2) }
                jsonPath("$.items.length()") { value(2) }
                jsonPath("$.items[0].id") { value("7") }
                jsonPath("$.items[0].name") { value("Instant Snack") }
                jsonPath("$.items[0].imageUrl") { value(null as Any?) }
                jsonPath("$.items[0].totalIngredients") { value(1) }
                jsonPath("$.items[0].matchedIngredients") { value(1) }
                jsonPath("$.items[0].missingIngredients.length()") { value(0) }
                jsonPath("$.items[0].readyToCook") { value(true) }
                jsonPath("$.items[1].missingIngredients[0]") { value("milk") }
            }

        verify {
            searchRecipes.execute(
                SearchRecipesQuery(
                    ingredientNames = listOf(IngredientName.of("flour"), IngredientName.of("eggs")),
                    readyToCookOnly = false,
                    page = 0,
                    size = 20,
                ),
            )
        }
    }

    @Test
    fun `search maps explicit optional params to the command`() {
        every { searchRecipes.execute(any()) } returns Ok(RecipeSearchResult(1, 5, 0, emptyList()))

        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                param("readyToCookOnly", "true")
                param("page", "1")
                param("size", "5")
            }.andExpect { status { isOk() } }

        verify {
            searchRecipes.execute(
                SearchRecipesQuery(
                    ingredientNames = listOf(IngredientName.of("eggs")),
                    readyToCookOnly = true,
                    page = 1,
                    size = 5,
                ),
            )
        }
    }

    @Test
    fun `search with blank ingredient list returns 422 error envelope`() {
        every { searchRecipes.execute(any()) } returns Err(UseCaseError.Unprocessable.EmptyIngredientList)

        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "")
            }.andExpect {
                status { isUnprocessableEntity() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(422) }
                jsonPath("$.error") { value("Unprocessable Entity") }
                jsonPath("$.path") { value("/api/recipes/search") }
                jsonPath("$.code") { value("EMPTY_INGREDIENT_LIST") }
                jsonPath("$.message") { isNotEmpty() }
            }
    }

    @Test
    fun `search without required ingredients param returns 400 error envelope`() {
        mockMvc
            .get("/api/recipes/search")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value(400) }
                jsonPath("$.code") { value("MISSING_PARAMETER") }
                jsonPath("$.path") { value("/api/recipes/search") }
            }
    }

    @Test
    fun `search with non-numeric page returns 400 error envelope`() {
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                param("page", "abc")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_PARAMETER") }
                jsonPath("$.path") { value("/api/recipes/search") }
            }
    }

    @Test
    fun `search with out-of-range page or size returns 400 error envelope`() {
        every { searchRecipes.execute(any()) } returns Ok(RecipeSearchResult(0, 0, 0, emptyList()))

        listOf(
            "page" to "-1",
            "page" to "10001",
            "size" to "0",
            "size" to "101",
            "size" to "999999",
        ).forEach { (name, value) ->
            mockMvc
                .get("/api/recipes/search") {
                    param("ingredients", "eggs")
                    param(name, value)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.status") { value(400) }
                    jsonPath("$.code") { value("INVALID_PARAMETER") }
                    jsonPath("$.path") { value("/api/recipes/search") }
                    jsonPath("$.message") { isNotEmpty() }
                }
        }
    }

    @Test
    fun `search accepts the boundary values of page and size`() {
        every { searchRecipes.execute(any()) } returns Ok(RecipeSearchResult(0, 0, 0, emptyList()))

        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                param("page", "0")
                param("size", "1")
            }.andExpect { status { isOk() } }
        mockMvc
            .get("/api/recipes/search") {
                param("ingredients", "eggs")
                param("page", "10000")
                param("size", "100")
            }.andExpect { status { isOk() } }

        verify {
            searchRecipes.execute(SearchRecipesQuery(listOf(IngredientName.of("eggs")), false, 0, 1))
            searchRecipes.execute(SearchRecipesQuery(listOf(IngredientName.of("eggs")), false, 10000, 100))
        }
    }

    @Test
    fun `get maps path id to the command and returns 200 with the detail dto`() {
        val detail =
            RecipeDetail(
                id = "1",
                name = "Perfect Pancakes",
                imageUrl = "http://img.example/pancakes.jpg",
                ingredients = listOf("butter", "egg", "flour", "milk", "sugar"),
                instructions = emptyList(),
            )
        every { getRecipe.execute(any()) } returns Ok(detail)

        mockMvc
            .get("/api/recipes/1")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value("1") }
                jsonPath("$.name") { value("Perfect Pancakes") }
                jsonPath("$.imageUrl") { value("http://img.example/pancakes.jpg") }
                jsonPath("$.ingredients.length()") { value(5) }
                jsonPath("$.ingredients[0]") { value("butter") }
                jsonPath("$.instructions.length()") { value(0) }
            }

        verify { getRecipe.execute(GetRecipeQuery(RecipeId(1))) }
    }

    @Test
    fun `get with unknown id returns 404 error envelope`() {
        every { getRecipe.execute(any()) } returns Err(UseCaseError.NotFound.RecipeNotFound)

        mockMvc
            .get("/api/recipes/42")
            .andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(404) }
                jsonPath("$.error") { value("Not Found") }
                jsonPath("$.path") { value("/api/recipes/42") }
                jsonPath("$.code") { value("RECIPE_NOT_FOUND") }
                jsonPath("$.message") { isNotEmpty() }
            }
    }

    @Test
    fun `get with non-numeric id returns 400 error envelope`() {
        mockMvc
            .get("/api/recipes/abc")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_PARAMETER") }
                jsonPath("$.path") { value("/api/recipes/abc") }
            }
    }

    @Test
    fun `internal use case error returns 500 error envelope`() {
        every { getRecipe.execute(any()) } returns Err(UseCaseError.Internal("boom"))

        mockMvc
            .get("/api/recipes/1")
            .andExpect {
                status { isInternalServerError() }
                jsonPath("$.status") { value(500) }
                jsonPath("$.code") { value("INTERNAL_ERROR") }
                jsonPath("$.path") { value("/api/recipes/1") }
            }
    }

    @Test
    fun `unexpected exception returns 500 error envelope`() {
        every { getRecipe.execute(any()) } throws IllegalStateException("boom")

        mockMvc
            .get("/api/recipes/1")
            .andExpect {
                status { isInternalServerError() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(500) }
                jsonPath("$.error") { value("Internal Server Error") }
                jsonPath("$.path") { value("/api/recipes/1") }
                jsonPath("$.code") { value("INTERNAL_ERROR") }
                jsonPath("$.message") { value("An unexpected error occurred") }
            }
    }

    @Test
    fun `POST on the get-only search endpoint yields 405 from spring, not the 500 envelope`() {
        mockMvc
            .post("/api/recipes/search")
            .andExpect {
                status { isMethodNotAllowed() }
                jsonPath("$.code") { doesNotExist() }
            }
    }

    @Test
    fun `GET with an unacceptable media type yields 406 from spring, not the 500 envelope`() {
        mockMvc
            .get("/api/recipes/1") {
                accept = MediaType.APPLICATION_XML
            }.andExpect {
                status { isNotAcceptable() }
                jsonPath("$.code") { doesNotExist() }
            }
    }

    @Test
    fun `GET on an unknown path yields 404 from spring, not the 500 envelope`() {
        mockMvc
            .get("/api/nope")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.code") { doesNotExist() }
            }
    }
}
