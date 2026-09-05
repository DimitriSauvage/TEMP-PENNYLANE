package com.pennylane.recipes.recipe.application.service

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.pennylane.recipes.recipe.application.command.SuggestIngredientsQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.model.IngredientSuggestions
import com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase
import com.pennylane.recipes.recipe.domain.port.outbound.IngredientCatalogPort

/** SuggestIngredientsUseCase: normalized prefix filter, capped suggestion list. */
class SuggestIngredientsService(
    private val catalog: IngredientCatalogPort,
) : SuggestIngredientsUseCase {
    override fun execute(query: SuggestIngredientsQuery): Result<IngredientSuggestions, UseCaseError> {
        val names = catalog.findNamesByPrefix(query.prefix).map { it.value }
        return Ok(IngredientSuggestions(names))
    }
}
