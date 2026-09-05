package com.pennylane.recipes.recipe.application.error

import com.pennylane.recipes.recipe.domain.error.RecipeDomainError
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DomainErrorMapperTest {
    @Test
    fun `map - every recipe domain error lands in its use case error group`() {
        val cases: List<Pair<RecipeDomainError, UseCaseError>> =
            listOf(
                RecipeDomainError.EmptyIngredientList to UseCaseError.Unprocessable.EmptyIngredientList,
            )
        cases.forEach { (domain, expected) ->
            val mapped = DomainErrorMapper.map(domain)
            assertThat(mapped).`as`("mapping of %s", domain::class.simpleName).isEqualTo(expected)
        }
    }

    @Test
    fun `map - preserves the domain error code and message`() {
        val mapped = DomainErrorMapper.map(RecipeDomainError.EmptyIngredientList)

        assertThat(mapped.code).isEqualTo(RecipeDomainError.EmptyIngredientList.code)
        assertThat(mapped.message).isEqualTo(RecipeDomainError.EmptyIngredientList.message)
    }
}
