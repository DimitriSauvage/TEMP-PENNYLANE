package com.pennylane.recipes.recipe.application.model

import com.pennylane.recipes.recipe.domain.model.RecipeMatch

/** One page of search results over the ranked, readiness-filtered match set. */
data class RecipeSearchResult(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val items: List<RecipeSearchItem>,
)

/**
 * A ranked search hit: recipe summary plus match statistics.
 * [totalIngredients] equals [matchedIngredients] + missing size.
 */
data class RecipeSearchItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val totalIngredients: Int,
    val matchedIngredients: Int,
    val missingIngredients: List<String>,
    val readyToCook: Boolean,
) {
    companion object {
        fun from(match: RecipeMatch): RecipeSearchItem =
            RecipeSearchItem(
                id =
                    match.recipe.id.value
                        .toString(),
                name = match.recipe.name,
                imageUrl = match.recipe.imageUrl,
                totalIngredients = match.recipe.ingredients.size,
                matchedIngredients = match.matched.size,
                missingIngredients = match.missing.map { it.value },
                readyToCook = match.readyToCook,
            )
    }
}
