package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.command.SuggestIngredientsQuery
import com.pennylane.recipes.recipe.domain.model.IngredientName

/** Raw suggestion request; [toCommand] is the only place the prefix becomes a domain value. */
data class IngredientSuggestRequestDto(
    val prefix: String,
) {
    fun toCommand(): SuggestIngredientsQuery = SuggestIngredientsQuery(IngredientName.of(prefix))
}
