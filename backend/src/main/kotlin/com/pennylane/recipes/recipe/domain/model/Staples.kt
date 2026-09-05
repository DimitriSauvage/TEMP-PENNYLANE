package com.pennylane.recipes.recipe.domain.model

/**
 * Ingredients assumed present in every household: they are never counted as
 * missing when a recipe is matched against the user's ingredient list.
 */
object Staples {
    val names: Set<IngredientName> =
        setOf("salt", "pepper", "water", "olive oil").map { IngredientName.of(it) }.toSet()
}
