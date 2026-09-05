package com.pennylane.recipes.recipe.application.error

import com.pennylane.recipes.recipe.domain.error.RecipeDomainError

/** Maps recipe domain errors to their application-level counterparts. */
object DomainErrorMapper {
    fun map(error: RecipeDomainError): UseCaseError =
        when (error) {
            RecipeDomainError.EmptyIngredientList -> UseCaseError.Unprocessable.EmptyIngredientList
        }
}
