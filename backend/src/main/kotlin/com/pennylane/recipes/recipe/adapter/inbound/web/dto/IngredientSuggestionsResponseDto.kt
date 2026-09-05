package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.model.IngredientSuggestions
import io.swagger.v3.oas.annotations.media.Schema

/** Catalog ingredient names matching a suggestion prefix, alphabetically ordered, capped at 10. */
@Schema(description = "Catalog ingredient names matching the suggestion prefix")
data class IngredientSuggestionsResponseDto(
    @field:Schema(description = "Matching ingredient names, alphabetically ordered, at most 10")
    val names: List<String>,
) {
    companion object {
        fun from(suggestions: IngredientSuggestions): IngredientSuggestionsResponseDto =
            IngredientSuggestionsResponseDto(names = suggestions.names)
    }
}
