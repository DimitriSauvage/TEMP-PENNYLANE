package com.pennylane.recipes.recipe.application.port.inbound

import com.github.michaelbull.result.Result
import com.pennylane.recipes.recipe.application.command.GetRecipeQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.RecipeDetail

/**
 * Loads the full detail of one recipe.
 *
 * Errors:
 * - [UseCaseError.NotFound.RecipeNotFound] — no recipe exists for the given id.
 */
fun interface GetRecipeUseCase {
    /** Executes the lookup. */
    fun execute(query: GetRecipeQuery): Result<RecipeDetail, UseCaseError>
}
