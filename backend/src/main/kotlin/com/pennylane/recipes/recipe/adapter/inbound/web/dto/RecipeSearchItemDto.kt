package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.model.RecipeSearchItem
import io.swagger.v3.oas.annotations.media.Schema

/** A ranked search hit with match statistics against the user's ingredients. */
@Schema(description = "One ranked search hit: recipe summary plus match statistics")
data class RecipeSearchItemDto(
    @field:Schema(description = "Recipe identifier", example = "8")
    val id: String,
    @field:Schema(description = "Recipe name", example = "Salted Eggs")
    val name: String,
    @field:Schema(description = "Recipe image URL; may be absent", nullable = true, example = "http://img.example/pancakes.jpg")
    val imageUrl: String?,
    @field:Schema(description = "Number of distinct ingredients the recipe needs", example = "2")
    val totalIngredients: Int,
    @field:Schema(description = "Number of recipe ingredients covered by the user list or pantry staples", example = "2")
    val matchedIngredients: Int,
    @field:Schema(description = "Normalized names of the recipe ingredients that are not covered")
    val missingIngredients: List<String>,
    @field:Schema(description = "True when nothing is missing, i.e. the recipe can be cooked right away", example = "true")
    val readyToCook: Boolean,
) {
    companion object {
        fun from(item: RecipeSearchItem): RecipeSearchItemDto =
            RecipeSearchItemDto(
                id = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                totalIngredients = item.totalIngredients,
                matchedIngredients = item.matchedIngredients,
                missingIngredients = item.missingIngredients,
                readyToCook = item.readyToCook,
            )
    }
}
