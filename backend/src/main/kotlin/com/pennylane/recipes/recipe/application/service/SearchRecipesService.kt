package com.pennylane.recipes.recipe.application.service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.pennylane.recipes.recipe.application.command.SearchRecipesQuery
import com.pennylane.recipes.recipe.application.error.DomainErrorMapper
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.RecipeSearchItem
import com.pennylane.recipes.recipe.application.model.RecipeSearchResult
import com.pennylane.recipes.recipe.application.port.inbound.SearchRecipesUseCase
import com.pennylane.recipes.recipe.domain.error.RecipeDomainError
import com.pennylane.recipes.recipe.domain.model.Staples
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeCatalogPort

/**
 * SearchRecipesUseCase: validates the pantry, then delegates ranking, readiness
 * filtering and pagination to the catalog port (SQL-side).
 */
class SearchRecipesService(
    private val catalog: RecipeCatalogPort,
) : SearchRecipesUseCase {
    override fun execute(query: SearchRecipesQuery): Result<RecipeSearchResult, UseCaseError> {
        val userIngredients =
            query.ingredientNames
                .filter { it.isValid }
                .toSet()
        if (userIngredients.isEmpty()) return Err(DomainErrorMapper.map(RecipeDomainError.EmptyIngredientList))

        val page = query.page.coerceAtLeast(0)
        val size = query.size.coerceAtLeast(1)
        val result = catalog.searchByPantry(userIngredients + Staples.names, query.readyToCookOnly, page, size)
        return Ok(
            RecipeSearchResult(
                page = page,
                size = size,
                totalElements = result.totalElements,
                items = result.matches.map(RecipeSearchItem::from),
            ),
        )
    }
}
