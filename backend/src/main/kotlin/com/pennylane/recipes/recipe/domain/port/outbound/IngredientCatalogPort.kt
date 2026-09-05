package com.pennylane.recipes.recipe.domain.port.outbound

import com.pennylane.recipes.recipe.domain.model.IngredientCatalog
import com.pennylane.recipes.recipe.domain.model.IngredientName

/**
 * Read access to the ingredient names known to the catalog.
 * Implemented by the persistence adapter in a later wave.
 *
 * Not a fun interface: the limit parameter needs a default value, which Kotlin
 * forbids on fun interface abstract methods.
 */
interface IngredientCatalogPort {
    /**
     * Returns the catalog ingredient names (normalized, distinct, alphabetical) starting
     * with [prefix], capped at [limit] ([IngredientCatalog.MAX_SUGGESTIONS] by default).
     */
    fun findNamesByPrefix(
        prefix: IngredientName,
        limit: Int = IngredientCatalog.MAX_SUGGESTIONS,
    ): List<IngredientName>
}
