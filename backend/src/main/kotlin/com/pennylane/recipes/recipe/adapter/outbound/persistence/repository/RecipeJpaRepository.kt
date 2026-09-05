package com.pennylane.recipes.recipe.adapter.outbound.persistence.repository

import com.pennylane.recipes.recipe.adapter.outbound.persistence.entity.RecipeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeJpaRepository : JpaRepository<RecipeEntity, Long>
