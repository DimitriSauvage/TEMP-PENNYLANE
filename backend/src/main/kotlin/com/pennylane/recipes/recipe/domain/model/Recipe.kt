package com.pennylane.recipes.recipe.domain.model

@JvmInline
value class RecipeId(
    val value: Long,
)

/**
 * A catalog recipe. Immutable read model: the catalog owns its lifecycle;
 * search matches it against a pantry (see [RecipeMatch]).
 */
class Recipe private constructor(
    val id: RecipeId,
    val name: String,
    val imageUrl: String?,
    val ingredients: List<IngredientName>,
    val instructions: List<String>,
) {
    companion object {
        /**
         * Rebuilds a recipe from catalog data. Ingredient names are normalized and
         * deduplicated (first occurrence kept) so matching operates on a canonical list.
         */
        fun rehydrate(
            id: RecipeId,
            name: String,
            imageUrl: String?,
            ingredients: List<String>,
            instructions: List<String>,
        ): Recipe =
            Recipe(
                id = id,
                name = name,
                imageUrl = imageUrl,
                ingredients = ingredients.map { IngredientName.of(it) }.distinct(),
                instructions = instructions,
            )
    }
}
