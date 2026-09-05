package com.pennylane.recipes.recipe.application.service

import com.pennylane.recipes.recipe.application.command.SuggestIngredientsQuery
import com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase
import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.port.outbound.IngredientCatalogPort
import com.pennylane.recipes.utils.assertOk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestIngredientsUseCaseTest {
    @Test
    fun `execute - prefix matches catalog names case-insensitively`() {
        val useCase = suggestUseCase("chicken breast", "chicken thigh", "chickpeas", "salmon")

        val result = useCase.execute(SuggestIngredientsQuery(prefix = IngredientName.of("  CHICK ")))

        result.assertOk { suggestions ->
            assertThat(suggestions.names)
                .containsExactly("chicken breast", "chicken thigh", "chickpea")
        }
    }

    @Test
    fun `execute - suggestions are limited to the maximum count`() {
        val useCase = suggestUseCase(*((1..12).map { "spice $it" }).toTypedArray())

        val result = useCase.execute(SuggestIngredientsQuery(prefix = IngredientName.of("spice")))

        result.assertOk { suggestions ->
            assertThat(suggestions.names).hasSize(10)
            assertThat(suggestions.names.first()).isEqualTo("spice 1")
        }
    }

    private fun suggestUseCase(vararg names: String): SuggestIngredientsUseCase =
        SuggestIngredientsService(
            object : IngredientCatalogPort {
                // Simulates the SQL contract of findByNameStartingWithOrderByNameAsc:
                // prefix match, alphabetical, capped at the caller's limit.
                override fun findNamesByPrefix(
                    prefix: IngredientName,
                    limit: Int,
                ): List<IngredientName> =
                    names
                        .map { IngredientName.of(it) }
                        .filter { it.value.startsWith(prefix.value) }
                        .take(limit)
            },
        )
}
