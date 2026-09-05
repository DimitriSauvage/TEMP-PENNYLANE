package com.pennylane.recipes.recipe.application.command

import com.pennylane.recipes.recipe.domain.model.IngredientName

/**
 * Input of [com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase];
 * [prefix] is an already-normalized domain value, wrapped by the adapter's [toCommand].
 */
data class SuggestIngredientsQuery(
    val prefix: IngredientName,
)
