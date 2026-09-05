package com.pennylane.recipes.recipe.application.port.inbound

import com.github.michaelbull.result.Result
import com.pennylane.recipes.recipe.application.command.SearchRecipesQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.RecipeSearchResult

/**
 * Ranks the recipe catalog against the ingredient names the user has at home:
 * fewest missing ingredients first, ties broken by most matched ingredients first.
 * Staples are never counted as missing.
 *
 * Errors:
 * - [UseCaseError.Unprocessable.EmptyIngredientList] — the ingredient list is empty
 *   (or blank-only after normalization); search is blocked.
 */
fun interface SearchRecipesUseCase {
    /** Executes the search; returns one page of ranked matches. */
    fun execute(query: SearchRecipesQuery): Result<RecipeSearchResult, UseCaseError>
}
