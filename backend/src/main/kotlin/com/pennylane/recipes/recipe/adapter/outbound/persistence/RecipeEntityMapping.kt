package com.pennylane.recipes.recipe.adapter.outbound.persistence

import com.pennylane.recipes.recipe.adapter.outbound.persistence.entity.RecipeEntity
import com.pennylane.recipes.recipe.domain.model.Recipe
import com.pennylane.recipes.recipe.domain.model.RecipeId

/**
 * Maps catalog rows to domain recipes. Ingredient names are stored already
 * normalized (the seeder canonicalizes through [com.pennylane.recipes.recipe.domain.model.IngredientName.of]);
 * recipes carry no instructions because the dataset contains none (documented product choice).
 */
internal fun RecipeEntity.toDomain(): Recipe =
    Recipe.rehydrate(
        id = RecipeId(requireNotNull(id)),
        name = name,
        imageUrl = imageUrl,
        ingredients = ingredients.map { it.name },
        instructions = emptyList(),
    )
