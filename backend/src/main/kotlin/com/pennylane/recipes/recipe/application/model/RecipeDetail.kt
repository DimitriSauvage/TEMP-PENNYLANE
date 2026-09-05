package com.pennylane.recipes.recipe.application.model

import com.pennylane.recipes.recipe.domain.model.Recipe

/** Full detail of a single recipe. */
data class RecipeDetail(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val ingredients: List<String>,
    val instructions: List<String>,
) {
    companion object {
        fun from(recipe: Recipe): RecipeDetail =
            RecipeDetail(
                id = recipe.id.value.toString(),
                name = recipe.name,
                imageUrl = recipe.imageUrl,
                ingredients = recipe.ingredients.map { it.value },
                instructions = recipe.instructions,
            )
    }
}
