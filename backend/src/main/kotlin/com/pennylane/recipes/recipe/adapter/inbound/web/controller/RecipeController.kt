package com.pennylane.recipes.recipe.adapter.inbound.web.controller

import com.github.michaelbull.result.fold
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.NotFoundResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.ServerErrorResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.SuccessResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.UnprocessableResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.ValidationErrorResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.dto.RecipeDetailRequestDto
import com.pennylane.recipes.recipe.adapter.inbound.web.dto.RecipeDetailResponseDto
import com.pennylane.recipes.recipe.adapter.inbound.web.dto.RecipeSearchRequestDto
import com.pennylane.recipes.recipe.adapter.inbound.web.dto.RecipeSearchResponseDto
import com.pennylane.recipes.recipe.adapter.inbound.web.errormanager.ApplicationException
import com.pennylane.recipes.recipe.application.port.inbound.GetRecipeUseCase
import com.pennylane.recipes.recipe.application.port.inbound.SearchRecipesUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Recipe search and detail endpoints; each method folds one use case and delegates errors to the advice.
 * Parameter constraints are enforced by Spring MVC's built-in method validation (no class-level
 * @Validated: it would trigger AOP method validation and bypass the HandlerMethodValidationException path).
 */
@Tag(name = "Recipes", description = "Search recipes by available ingredients and fetch recipe details")
@RestController
@RequestMapping("/api/recipes")
class RecipeController(
    private val searchRecipes: SearchRecipesUseCase,
    private val getRecipe: GetRecipeUseCase,
) {
    @Operation(
        summary = "Search recipes by available ingredients",
        description =
            "Ranks the whole catalog against the comma-separated ingredients the user has, counting pantry " +
                "staples (salt, pepper, water, olive oil) as always available. Recipes with the fewest missing " +
                "ingredients come first; ties are broken by most matched ingredients. The result is paginated over " +
                "the ranked list. Running the search without at least one usable ingredient is rejected with 422.",
    )
    @SuccessResponseDoc
    @ValidationErrorResponseDoc
    @UnprocessableResponseDoc
    @ServerErrorResponseDoc
    @GetMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun search(
        @Parameter(
            description = "Comma-separated ingredient names the user has; case and plural forms are normalized",
            example = "flour,eggs",
            required = true,
            `in` = ParameterIn.QUERY,
        )
        @RequestParam ingredients: String,
        @Parameter(
            description = "Keep only recipes with no missing ingredient (ready to cook right away)",
            example = "true",
            `in` = ParameterIn.QUERY,
        )
        @RequestParam(required = false) readyToCookOnly: Boolean?,
        @Parameter(
            description = "Zero-based page index over the ranked matches",
            example = "0",
            `in` = ParameterIn.QUERY,
        )
        @RequestParam(required = false)
        @Min(0)
        @Max(10_000) page: Int?,
        @Parameter(
            description = "Number of matches per page",
            example = "20",
            `in` = ParameterIn.QUERY,
        )
        @RequestParam(required = false)
        @Min(1)
        @Max(100) size: Int?,
    ): ResponseEntity<RecipeSearchResponseDto> {
        val command = RecipeSearchRequestDto(ingredients, readyToCookOnly, page, size).toCommand()
        return searchRecipes.execute(command).fold(
            success = { ResponseEntity.ok(RecipeSearchResponseDto.from(it)) },
            failure = { throw ApplicationException(it) },
        )
    }

    @Operation(
        summary = "Get a recipe by id",
        description =
            "Returns the full recipe detail: normalized, deduplicated ingredient names and the " +
                "preparation instructions in order.",
    )
    @SuccessResponseDoc
    @ValidationErrorResponseDoc
    @NotFoundResponseDoc
    @ServerErrorResponseDoc
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun get(
        @Parameter(
            description = "Unique numeric identifier of the recipe",
            example = "1",
            required = true,
            `in` = ParameterIn.PATH,
        )
        @PathVariable id: Long,
    ): ResponseEntity<RecipeDetailResponseDto> {
        val command = RecipeDetailRequestDto(id).toCommand()
        return getRecipe.execute(command).fold(
            success = { ResponseEntity.ok(RecipeDetailResponseDto.from(it)) },
            failure = { throw ApplicationException(it) },
        )
    }
}
