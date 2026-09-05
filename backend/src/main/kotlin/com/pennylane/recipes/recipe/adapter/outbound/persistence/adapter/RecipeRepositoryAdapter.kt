package com.pennylane.recipes.recipe.adapter.outbound.persistence.adapter

import com.pennylane.recipes.recipe.adapter.outbound.persistence.repository.RecipeJpaRepository
import com.pennylane.recipes.recipe.adapter.outbound.persistence.toDomain
import com.pennylane.recipes.recipe.domain.model.Recipe
import com.pennylane.recipes.recipe.domain.model.RecipeId
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeRepositoryPort
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
open class RecipeRepositoryAdapter(
    private val recipes: RecipeJpaRepository,
) : RecipeRepositoryPort {
    override fun findById(id: RecipeId): Recipe? = recipes.findById(id.value).map { it.toDomain() }.orElse(null)
}
