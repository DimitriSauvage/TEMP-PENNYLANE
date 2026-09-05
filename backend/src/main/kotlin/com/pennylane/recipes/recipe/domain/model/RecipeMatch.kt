package com.pennylane.recipes.recipe.domain.model

/**
 * Outcome of matching one recipe against the user's ingredient list.
 * matched + missing always equals the recipe's (deduplicated) ingredient count.
 */
data class RecipeMatch(
    val recipe: Recipe,
    val matched: List<IngredientName>,
    val missing: List<IngredientName>,
) {
    val readyToCook: Boolean
        get() = missing.isEmpty()
}
