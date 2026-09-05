package com.pennylane.recipes.recipe.adapter.outbound.persistence.repository

import com.pennylane.recipes.recipe.adapter.outbound.persistence.entity.IngredientEntity
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository

interface IngredientJpaRepository : JpaRepository<IngredientEntity, Long> {
    // The suggestion cap is passed by the caller (the port defaults it to
    // IngredientCatalog.MAX_SUGGESTIONS); LIKE 'prefix%' range-scans uk_ingredient_name.
    fun findByNameStartingWithOrderByNameAsc(
        name: String,
        limit: Limit,
    ): List<IngredientEntity>
}
