package com.pennylane.recipes.recipe.application.service

import com.pennylane.recipes.recipe.application.command.SearchRecipesQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.model.Recipe
import com.pennylane.recipes.recipe.domain.model.RecipeId
import com.pennylane.recipes.recipe.domain.model.RecipeMatch
import com.pennylane.recipes.recipe.domain.model.RecipeSearchPage
import com.pennylane.recipes.recipe.domain.model.Staples
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeCatalogPort
import com.pennylane.recipes.utils.assertErr
import com.pennylane.recipes.utils.assertOk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Service-level behavior: pantry composition, blank filtering, typed errors,
 * parameter passing and result mapping. Ranking semantics live in the adapter's
 * SQL and are pinned by RecipeSearchIT against real Postgres.
 */
class SearchRecipesUseCaseTest {
    private var nextId = 1L

    @Test
    fun `execute - empty ingredient list returns typed error without querying the catalog`() {
        val catalog = FakeCatalog()
        val useCase = SearchRecipesService(catalog)

        val result = useCase.execute(SearchRecipesQuery(ingredientNames = emptyList()))

        result.assertErr(UseCaseError.Unprocessable.EmptyIngredientList)
        assertThat(catalog.calls).isEmpty()
    }

    @Test
    fun `execute - blank-only ingredient list returns typed error without querying the catalog`() {
        val catalog = FakeCatalog()
        val useCase = SearchRecipesService(catalog)

        val result = useCase.execute(SearchRecipesQuery(ingredientNames = names("   ")))

        result.assertErr(UseCaseError.Unprocessable.EmptyIngredientList)
        assertThat(catalog.calls).isEmpty()
    }

    @Test
    fun `execute - pantry is the user's non-blank ingredients plus staples`() {
        val catalog = FakeCatalog()
        val useCase = SearchRecipesService(catalog)

        useCase.execute(SearchRecipesQuery(ingredientNames = names("flour", "eggs", "  ")))

        val call = catalog.calls.single()
        assertThat(call.pantry)
            .isEqualTo(setOf(IngredientName.of("flour"), IngredientName.of("egg")) + Staples.names)
        assertThat(call.readyToCookOnly).isFalse()
        assertThat(call.page).isEqualTo(0)
        assertThat(call.size).isEqualTo(20)
    }

    @Test
    fun `execute - readyToCookOnly, page and size are passed through`() {
        val catalog = FakeCatalog()
        val useCase = SearchRecipesService(catalog)

        useCase.execute(
            SearchRecipesQuery(
                ingredientNames = names("flour"),
                readyToCookOnly = true,
                page = 2,
                size = 7,
            ),
        )

        val call = catalog.calls.single()
        assertThat(call.readyToCookOnly).isTrue()
        assertThat(call.page).isEqualTo(2)
        assertThat(call.size).isEqualTo(7)
    }

    @Test
    fun `execute - negative page and size are coerced before the catalog call`() {
        val catalog = FakeCatalog()
        val useCase = SearchRecipesService(catalog)

        useCase.execute(SearchRecipesQuery(ingredientNames = names("flour"), page = -5, size = 0))

        val call = catalog.calls.single()
        assertThat(call.page).isEqualTo(0)
        assertThat(call.size).isEqualTo(1)
    }

    @Test
    fun `execute - maps the catalog page into the search result`() {
        val catalog =
            FakeCatalog(
                RecipeSearchPage(
                    totalElements = 42,
                    matches =
                        listOf(
                            match(
                                name = "Pancakes",
                                ingredients = listOf("flour", "milk"),
                                matched = listOf("flour"),
                            ),
                        ),
                ),
            )
        val useCase = SearchRecipesService(catalog)

        useCase.execute(SearchRecipesQuery(ingredientNames = names("flour"))).assertOk { result ->
            assertThat(result.page).isEqualTo(0)
            assertThat(result.size).isEqualTo(20)
            assertThat(result.totalElements).isEqualTo(42)
            val item = result.items.single()
            assertThat(item.id).isEqualTo("1")
            assertThat(item.name).isEqualTo("Pancakes")
            assertThat(item.totalIngredients).isEqualTo(2)
            assertThat(item.matchedIngredients).isEqualTo(1)
            assertThat(item.missingIngredients).containsExactly("milk")
            assertThat(item.readyToCook).isFalse()
        }
    }

    private fun names(vararg raw: String): List<IngredientName> = raw.map { IngredientName.of(it) }

    private fun match(
        name: String,
        ingredients: List<String>,
        matched: List<String>,
    ): RecipeMatch {
        val recipe =
            Recipe.rehydrate(
                id = RecipeId(nextId++),
                name = name,
                imageUrl = null,
                ingredients = ingredients,
                instructions = emptyList(),
            )
        val matchedSet = matched.map { IngredientName.of(it) }.toSet()
        return RecipeMatch(
            recipe = recipe,
            matched = recipe.ingredients.filter { it in matchedSet },
            missing = recipe.ingredients.filterNot { it in matchedSet },
        )
    }

    private data class CatalogCall(
        val pantry: Set<IngredientName>,
        val readyToCookOnly: Boolean,
        val page: Int,
        val size: Int,
    )

    private class FakeCatalog(
        private val result: RecipeSearchPage = RecipeSearchPage(0, emptyList()),
    ) : RecipeCatalogPort {
        val calls = mutableListOf<CatalogCall>()

        override fun searchByPantry(
            pantry: Set<IngredientName>,
            readyToCookOnly: Boolean,
            page: Int,
            size: Int,
        ): RecipeSearchPage {
            calls += CatalogCall(pantry, readyToCookOnly, page, size)
            return result
        }
    }
}
