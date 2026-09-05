package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.command.SearchRecipesQuery
import com.pennylane.recipes.recipe.domain.model.IngredientName

/** Raw search request parameters; [toCommand] is the only place they become the application command. */
data class RecipeSearchRequestDto(
    val ingredients: String,
    val readyToCookOnly: Boolean? = null,
    val page: Int? = null,
    val size: Int? = null,
) {
    fun toCommand(): SearchRecipesQuery =
        SearchRecipesQuery(
            ingredientNames = ingredients.split(",").map { IngredientName.of(it.trim()) },
            readyToCookOnly = readyToCookOnly ?: false,
            page = page ?: 0,
            size = size ?: 20,
        )
}
