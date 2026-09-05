package com.pennylane.recipes.recipe.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.pennylane.recipes.recipe.adapter.outbound.persistence.adapter.IngredientCatalogAdapter
import com.pennylane.recipes.recipe.adapter.outbound.persistence.adapter.RecipeCatalogAdapter
import com.pennylane.recipes.recipe.adapter.outbound.persistence.adapter.RecipeRepositoryAdapter
import com.pennylane.recipes.recipe.adapter.outbound.persistence.repository.IngredientJpaRepository
import com.pennylane.recipes.recipe.adapter.outbound.persistence.repository.RecipeJpaRepository
import com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding.RecipeSeedRunner
import com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding.RecipeSeeder
import com.pennylane.recipes.recipe.domain.port.outbound.IngredientCatalogPort
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeCatalogPort
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeRepositoryPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Configuration
class PersistenceConfig {
    @Bean
    fun recipeCatalogAdapter(jdbc: NamedParameterJdbcTemplate): RecipeCatalogPort = RecipeCatalogAdapter(jdbc)

    @Bean
    fun recipeRepositoryAdapter(recipes: RecipeJpaRepository): RecipeRepositoryPort = RecipeRepositoryAdapter(recipes)

    @Bean
    fun ingredientCatalogAdapter(ingredients: IngredientJpaRepository): IngredientCatalogPort = IngredientCatalogAdapter(ingredients)

    @Bean
    fun recipeSeeder(
        jdbcTemplate: JdbcTemplate,
        transactionManager: PlatformTransactionManager,
        objectMapper: ObjectMapper,
        resourceLoader: ResourceLoader,
        @Value("\${app.seed.url}") seedUrl: String,
    ): RecipeSeeder = RecipeSeeder(jdbcTemplate, TransactionTemplate(transactionManager), objectMapper, resourceLoader, seedUrl)

    @Bean
    fun recipeSeedRunner(
        jdbcTemplate: JdbcTemplate,
        seeder: RecipeSeeder,
    ): ApplicationRunner = RecipeSeedRunner(jdbcTemplate, seeder)
}
