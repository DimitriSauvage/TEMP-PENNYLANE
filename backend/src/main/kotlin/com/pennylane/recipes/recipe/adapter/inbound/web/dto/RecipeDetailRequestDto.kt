package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.command.GetRecipeQuery
import com.pennylane.recipes.recipe.domain.model.RecipeId

/** Raw recipe-detail request; [toCommand] is the only place the id becomes a [RecipeId]. */
data class RecipeDetailRequestDto(
    val id: Long,
) {
    fun toCommand(): GetRecipeQuery = GetRecipeQuery(RecipeId(id))
}
