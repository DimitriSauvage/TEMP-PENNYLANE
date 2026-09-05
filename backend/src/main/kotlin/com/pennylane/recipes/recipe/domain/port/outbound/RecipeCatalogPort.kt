package com.pennylane.recipes.recipe.domain.port.outbound

import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.model.RecipeSearchPage

/**
 * Read access to the recipe catalog for search-time matching.
 * Implemented by the persistence adapter.
 */
fun interface RecipeCatalogPort {
    /**
     * Returns the [page]-th slice (0-based, [size] rows) of the catalog ranked
     * against [pantry]: fewest missing ingredients first, ties broken by most
     * matched, then catalog order. [pantry] must already include staples.
     * With [readyToCookOnly], only recipes with no missing ingredient are
     * returned and counted; otherwise every recipe appears, even with zero
     * matched pantry ingredients.
     */
    fun searchByPantry(
        pantry: Set<IngredientName>,
        readyToCookOnly: Boolean,
        page: Int,
        size: Int,
    ): RecipeSearchPage
}
