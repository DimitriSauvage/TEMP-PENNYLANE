package com.pennylane.recipes.recipe.domain.port.outbound

import com.pennylane.recipes.recipe.domain.model.Recipe
import com.pennylane.recipes.recipe.domain.model.RecipeId

/**
 * Read access to a single recipe by identity.
 * Implemented by the persistence adapter in a later wave.
 */
fun interface RecipeRepositoryPort {
    /** Returns the recipe with [id], or null when no recipe exists for it. */
    fun findById(id: RecipeId): Recipe?
}
