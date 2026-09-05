package com.pennylane.recipes.recipe.domain.error

sealed class DomainError {
    abstract val code: String
    abstract val message: String
}
