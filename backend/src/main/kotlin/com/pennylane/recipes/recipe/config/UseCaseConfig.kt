package com.pennylane.recipes.recipe.config

import com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase
import com.pennylane.recipes.recipe.application.port.inbound.SearchRecipesUseCase
import com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase
import com.pennylane.recipes.recipe.application.service.GetRecipeService
import com.pennylane.recipes.recipe.application.service.SearchRecipesService
import com.pennylane.recipes.recipe.application.service.SuggestIngredientsService
import com.pennylane.recipes.recipe.domain.port.outbound.IngredientCatalogPort
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeCatalogPort
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeRepositoryPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig {
    @Bean
    fun searchRecipesUseCase(catalog: RecipeCatalogPort): SearchRecipesUseCase = SearchRecipesService(catalog)

    @Bean
    fun getRecipeUseCase(repository: RecipeRepositoryPort): GetRecipeUseCase = GetRecipeService(repository)

    @Bean
    fun suggestIngredientsUseCase(catalog: IngredientCatalogPort): SuggestIngredientsUseCase = SuggestIngredientsService(catalog)
}
