package com.pennylane.recipes.recipe.adapter.outbound.persistence

import com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding.RecipeSeeder
import com.pennylane.recipes.recipe.application.command.GetRecipeQuery
import com.pennylane.recipes.recipe.application.command.SearchRecipesQuery
import com.pennylane.recipes.recipe.application.command.SuggestIngredientsQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase
import com.pennylane.recipes.recipe.application.port.inbound.SearchRecipesUseCase
import com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase
import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.model.RecipeId
import com.pennylane.recipes.utils.assertErr
import com.pennylane.recipes.utils.assertOk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * Search, get and suggest use cases against a real Postgres catalog seeded
 * from a small fixture. Ranking, readiness filtering and pagination run in
 * SQL (RecipeCatalogAdapter); these tests pin that behavior to the semantics
 * of the former in-memory ranking.
 */
@SpringBootTest(
    properties = [
        "app.seed.url=classpath:seeding/search-fixture.json",
    ],
)
class RecipeSearchIT(
    private val searchRecipes: SearchRecipesUseCase,
    private val getRecipe: GetRecipeUseCase,
    private val suggestIngredients: SuggestIngredientsUseCase,
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
    fun `search ranks fewest missing first, then most matched`() {
        searchRecipes
            .execute(SearchRecipesQuery(ingredientNames = ingredients("flour", "eggs"), size = 20))
            .assertOk { result ->
                assertThat(result.totalElements).isEqualTo(9)
                val names = result.items.map { it.name }
                // missing 0: Salted Eggs matches 2 (egg + staple salt) before Instant Snack's 1
                assertThat(names[0]).isEqualTo("Salted Eggs")
                assertThat(names[1]).isEqualTo("Instant Snack")
                // missing 1, both matched 2 (salt/water are staples): stable catalog order
                assertThat(names[2]).isEqualTo("Simple Omelette")
                assertThat(names[3]).isEqualTo("Basic Bread")
                // missing 2: Tie Recipe B matches 2 before the matched-1 pair (tie-break)
                assertThat(names[4]).isEqualTo("Tie Recipe B")
                assertThat(names.subList(5, 7)).containsExactly("Flourless Chocolate Cake", "Tie Recipe A")
                assertThat(names[7]).isEqualTo("Perfect Pancakes")
                assertThat(names[8]).isEqualTo("Suggestion Bucket")
            }
    }

    @Test
    fun `search tie-breaks equal missing counts by most matched`() {
        // Tie Recipe A [flour, butter, sugar] and Tie Recipe B [flour, egg, butter, sugar]
        // both miss butter+sugar against [flour, eggs]; B matches 2, A matches 1.
        searchRecipes
            .execute(SearchRecipesQuery(ingredientNames = ingredients("flour", "eggs"), size = 20))
            .assertOk { result ->
                val missingTwo = result.items.filter { it.missingIngredients.size == 2 }
                assertThat(missingTwo).hasSize(3)
                assertThat(missingTwo[0].matchedIngredients).isEqualTo(2)
                assertThat(missingTwo.last().name).isEqualTo("Tie Recipe A")
            }
    }

    @Test
    fun `search treats staples as available - recipe needing eggs and salt misses nothing`() {
        searchRecipes
            .execute(
                SearchRecipesQuery(
                    ingredientNames = ingredients("eggs"),
                    readyToCookOnly = true,
                    size = 20,
                ),
            ).assertOk { result ->
                assertThat(result.items.map { it.name }).containsExactlyInAnyOrder(
                    "Instant Snack",
                    "Salted Eggs",
                )
                val salted = result.items.first { it.name == "Salted Eggs" }
                assertThat(salted.missingIngredients).isEmpty()
                assertThat(salted.readyToCook).isTrue()
            }
    }

    @Test
    fun `search paginates over real rows`() {
        searchRecipes
            .execute(SearchRecipesQuery(ingredientNames = ingredients("flour", "eggs"), page = 1, size = 3))
            .assertOk { result ->
                assertThat(result.totalElements).isEqualTo(9)
                assertThat(result.items.map { it.name }).containsExactly("Basic Bread", "Tie Recipe B", "Flourless Chocolate Cake")
            }
        searchRecipes
            .execute(SearchRecipesQuery(ingredientNames = ingredients("flour", "eggs"), page = 3, size = 3))
            .assertOk { result ->
                assertThat(result.totalElements).isEqualTo(9)
                assertThat(result.items).isEmpty()
            }
    }

    @Test
    fun `get recipe returns full detail with image`() {
        getRecipe.execute(GetRecipeQuery(RecipeId(1))).assertOk { detail ->
            assertThat(detail.name).isEqualTo("Perfect Pancakes")
            assertThat(detail.imageUrl).isEqualTo("http://img.example/pancakes.jpg")
            assertThat(detail.ingredients).containsExactly("butter", "egg", "flour", "milk", "sugar")
            // documented product choice: the dataset contains no instructions
            assertThat(detail.instructions).isEmpty()
        }
    }

    @Test
    fun `get recipe with unknown id yields typed not-found error`() {
        getRecipe.execute(GetRecipeQuery(RecipeId(999_999))).assertErr(UseCaseError.NotFound.RecipeNotFound)
    }

    @Test
    fun `suggest ingredients matches prefix case-insensitively and caps at 10`() {
        suggestIngredients.execute(SuggestIngredientsQuery(IngredientName.of("Chi"))).assertOk { suggestions ->
            assertThat(suggestions.names).containsExactly(
                "chicken",
                "chicken broth",
                "chicken stock",
                "chickpea",
                "chickpea flour",
                "chickpea pasta",
                "chile pepper",
                "chili paste",
                "chili powder",
                "chili sauce",
            )
        }
    }

    private fun ingredients(vararg raw: String): List<IngredientName> = raw.map { IngredientName.of(it) }
}
