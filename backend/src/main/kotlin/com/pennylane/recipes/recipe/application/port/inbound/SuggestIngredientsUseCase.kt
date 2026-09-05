package com.pennylane.recipes.recipe.application.port.inbound

import com.github.michaelbull.result.Result
import com.pennylane.recipes.recipe.application.command.SuggestIngredientsQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.IngredientSuggestions

/**
 * Suggests catalog ingredient names matching a prefix (case-insensitive),
 * limited to [com.pennylane.recipes.recipe.domain.model.IngredientCatalog.MAX_SUGGESTIONS].
 *
 * Never fails: a prefix with no match yields an empty suggestion list.
 */
fun interface SuggestIngredientsUseCase {
    /** Executes the suggestion. */
    fun execute(query: SuggestIngredientsQuery): Result<IngredientSuggestions, UseCaseError>
}
