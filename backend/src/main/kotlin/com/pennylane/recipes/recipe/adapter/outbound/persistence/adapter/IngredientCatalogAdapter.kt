package com.pennylane.recipes.recipe.adapter.outbound.persistence.adapter

import com.pennylane.recipes.recipe.adapter.outbound.persistence.repository.IngredientJpaRepository
import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.port.outbound.IngredientCatalogPort
import org.springframework.data.domain.Limit
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
open class IngredientCatalogAdapter(
    private val ingredients: IngredientJpaRepository,
) : IngredientCatalogPort {
    override fun findNamesByPrefix(
        prefix: IngredientName,
        limit: Int,
    ): List<IngredientName> =
        ingredients
            .findByNameStartingWithOrderByNameAsc(prefix.value, Limit.of(limit.coerceAtLeast(1)))
            .map { IngredientName(it.name) }
}
