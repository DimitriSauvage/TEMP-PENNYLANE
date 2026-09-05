package com.pennylane.recipes.recipe.application.service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.pennylane.recipes.recipe.application.command.GetRecipeQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.RecipeDetail
import com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeRepositoryPort

/** GetRecipeUseCase: single lookup, typed not-found error. */
class GetRecipeService(
    private val repository: RecipeRepositoryPort,
) : GetRecipeUseCase {
    override fun execute(query: GetRecipeQuery): Result<RecipeDetail, UseCaseError> {
        val recipe = repository.findById(query.recipeId) ?: return Err(UseCaseError.NotFound.RecipeNotFound)
        return Ok(RecipeDetail.from(recipe))
    }
}
