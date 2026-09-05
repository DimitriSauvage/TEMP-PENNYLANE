package com.pennylane.recipes.recipe.application.command

import com.pennylane.recipes.recipe.domain.model.IngredientName

/**
 * Input of [com.pennylane.recipes.recipe.application.port.inbound.SearchRecipesUseCase].
 * [ingredientNames] are already-normalized domain values; the adapter's [toCommand]
 * is the only place raw user input becomes an [IngredientName]. Pagination applies
 * to the ranked, readiness-filtered result set (0-based [page], [size] items per page).
 */
data class SearchRecipesQuery(
    val ingredientNames: List<IngredientName>,
    val readyToCookOnly: Boolean = false,
    val page: Int = 0,
    val size: Int = 20,
)
