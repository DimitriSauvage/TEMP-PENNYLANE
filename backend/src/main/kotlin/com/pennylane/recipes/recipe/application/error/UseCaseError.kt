package com.pennylane.recipes.recipe.application.error

sealed class UseCaseError {
    abstract val code: String
    abstract val message: String

    sealed class NotFound : UseCaseError() {
        data object RecipeNotFound : NotFound() {
            override val code = "RECIPE_NOT_FOUND"
            override val message = "Recipe not found"
        }
    }

    sealed class Unprocessable : UseCaseError() {
        data object EmptyIngredientList : Unprocessable() {
            override val code = "EMPTY_INGREDIENT_LIST"
            override val message = "The ingredient list must contain at least one ingredient"
        }
    }

    /**
     * Reserved fallback for a domain rule violation that is neither a not-found nor an
     * unprocessable-content condition (e.g. a malformed persisted value surfacing as a
     * typed error). Not constructed by any current use case; kept so the exhaustive
     * `when`s in [com.pennylane.recipes.recipe.adapter.inbound.web.errormanager.ApplicationErrorHandler]
     * force new domain errors to be wired through a group rather than silently reusing
     * this one. Maps to HTTP 400.
     */
    data class DomainValidation(
        override val code: String,
        override val message: String,
    ) : UseCaseError()

    /**
     * Reserved catch-all for an unexpected application-layer failure that is still worth
     * reporting as a typed error. Not constructed by any current use case; kept per the
     * two-layer error model so unexpected failures have a typed home. Maps to HTTP 500.
     */
    data class Internal(
        override val message: String,
    ) : UseCaseError() {
        override val code = "INTERNAL_ERROR"
    }
}
