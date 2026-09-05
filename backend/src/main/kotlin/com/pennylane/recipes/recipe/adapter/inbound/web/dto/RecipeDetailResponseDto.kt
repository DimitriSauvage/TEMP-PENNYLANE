package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.model.RecipeDetail
import io.swagger.v3.oas.annotations.media.Schema

/** Full detail of a single recipe. */
@Schema(description = "Full detail of a single recipe")
data class RecipeDetailResponseDto(
    @field:Schema(description = "Recipe identifier", example = "1")
    val id: String,
    @field:Schema(description = "Recipe name", example = "Perfect Pancakes")
    val name: String,
    @field:Schema(description = "Recipe image URL; may be absent", nullable = true, example = "http://img.example/pancakes.jpg")
    val imageUrl: String?,
    @field:Schema(description = "Normalized, deduplicated ingredient names", example = "[\"butter\",\"egg\",\"flour\"]")
    val ingredients: List<String>,
    @field:Schema(description = "Preparation instructions in order")
    val instructions: List<String>,
) {
    companion object {
        fun from(detail: RecipeDetail): RecipeDetailResponseDto =
            RecipeDetailResponseDto(
                id = detail.id,
                name = detail.name,
                imageUrl = detail.imageUrl,
                ingredients = detail.ingredients,
                instructions = detail.instructions,
            )
    }
}
