package com.pennylane.recipes.recipe.adapter.inbound.web.dto

import com.pennylane.recipes.recipe.application.model.RecipeSearchResult
import io.swagger.v3.oas.annotations.media.Schema

/** One page of recipe search results over the ranked, readiness-filtered match set. */
@Schema(description = "One page of recipe search results ranked by fewest missing ingredients first")
data class RecipeSearchResponseDto(
    @field:Schema(description = "Zero-based page index", example = "0")
    val page: Int,
    @field:Schema(description = "Requested page size", example = "20")
    val size: Int,
    @field:Schema(description = "Total number of ranked matches across all pages", example = "9")
    val totalElements: Int,
    @field:Schema(description = "Ranked recipes on this page")
    val items: List<RecipeSearchItemDto>,
) {
    companion object {
        fun from(result: RecipeSearchResult): RecipeSearchResponseDto =
            RecipeSearchResponseDto(
                page = result.page,
                size = result.size,
                totalElements = result.totalElements,
                items = result.items.map(RecipeSearchItemDto::from),
            )
    }
}
