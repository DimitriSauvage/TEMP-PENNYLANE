package com.pennylane.recipes.recipe.domain.model

/**
 * One page of pantry-ranked search results: [matches] holds the page's rows in
 * rank order (fewest missing ingredients first, then most matched, then catalog
 * order), [totalElements] counts every recipe that passed the readiness filter.
 */
data class RecipeSearchPage(
    val totalElements: Int,
    val matches: List<RecipeMatch>,
)
