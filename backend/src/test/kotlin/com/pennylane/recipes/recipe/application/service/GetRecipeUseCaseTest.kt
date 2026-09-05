package com.pennylane.recipes.recipe.application.service

import com.pennylane.recipes.recipe.application.command.GetRecipeQuery
import com.pennylane.recipes.recipe.application.error.UseCaseError
import com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase
import com.pennylane.recipes.recipe.domain.model.Recipe
import com.pennylane.recipes.recipe.domain.model.RecipeId
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeRepositoryPort
import com.pennylane.recipes.utils.assertErr
import com.pennylane.recipes.utils.assertOk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetRecipeUseCaseTest {
    private val useCase: GetRecipeUseCase =
        GetRecipeService(
            object : RecipeRepositoryPort {
                override fun findById(id: RecipeId): Recipe? = if (id.value == 1L) pancake() else null
            },
        )

    @Test
    fun `execute - known id returns full recipe detail`() {
        val result = useCase.execute(GetRecipeQuery(RecipeId(1L)))

        result.assertOk { detail ->
            assertThat(detail.id).isEqualTo("1")
            assertThat(detail.name).isEqualTo("Pancakes")
            assertThat(detail.imageUrl).isEqualTo("https://img.example/pancakes.jpg")
            assertThat(detail.ingredients).containsExactly("flour", "milk", "egg")
            assertThat(detail.instructions).containsExactly("Mix everything", "Cook on a hot pan")
        }
    }

    @Test
    fun `execute - unknown id returns RecipeNotFound error`() {
        val result = useCase.execute(GetRecipeQuery(RecipeId(42L)))

        result.assertErr(UseCaseError.NotFound.RecipeNotFound)
    }

    private fun pancake(): Recipe =
        Recipe.rehydrate(
            id = RecipeId(1L),
            name = "Pancakes",
            imageUrl = "https://img.example/pancakes.jpg",
            ingredients = listOf("flour", "milk", "egg"),
            instructions = listOf("Mix everything", "Cook on a hot pan"),
        )
}
