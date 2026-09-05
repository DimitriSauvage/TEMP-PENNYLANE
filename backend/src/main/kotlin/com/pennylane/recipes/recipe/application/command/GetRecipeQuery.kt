package com.pennylane.recipes.recipe.application.command

import com.pennylane.recipes.recipe.domain.model.RecipeId

/** Input of [com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase]. */
data class GetRecipeQuery(
    val recipeId: RecipeId,
)
