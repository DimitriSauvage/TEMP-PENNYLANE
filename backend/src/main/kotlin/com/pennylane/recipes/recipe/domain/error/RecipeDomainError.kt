package com.pennylane.recipes.recipe.domain.error

sealed class RecipeDomainError : DomainError() {
    data object EmptyIngredientList : RecipeDomainError() {
        override val code = "EMPTY_INGREDIENT_LIST"
        override val message = "The ingredient list must contain at least one ingredient"
    }
}
