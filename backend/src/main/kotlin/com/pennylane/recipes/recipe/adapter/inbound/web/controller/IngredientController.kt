package com.pennylane.recipes.recipe.adapter.inbound.web.controller

import com.github.michaelbull.result.fold
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.ServerErrorResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.SuccessResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.doc.ValidationErrorResponseDoc
import com.pennylane.recipes.recipe.adapter.inbound.web.dto.IngredientSuggestRequestDto
import com.pennylane.recipes.recipe.adapter.inbound.web.dto.IngredientSuggestionsResponseDto
import com.pennylane.recipes.recipe.adapter.inbound.web.errormanager.ApplicationException
import com.pennylane.recipes.recipe.application.port.inbound.SuggestIngredientsUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Ingredient autocomplete endpoint over the recipe catalog. */
@Tag(name = "Ingredients", description = "Ingredient autocomplete over the recipe catalog")
@RestController
@RequestMapping("/api/ingredients")
class IngredientController(
    private val suggestIngredients: SuggestIngredientsUseCase,
) {
    @Operation(
        summary = "Suggest ingredient names by prefix",
        description =
            "Case-insensitive prefix search over the catalog's normalized ingredient names, ordered " +
                "alphabetically and capped at 10 suggestions. A prefix without any match yields an empty list.",
    )
    @SuccessResponseDoc
    @ValidationErrorResponseDoc
    @ServerErrorResponseDoc
    @GetMapping("/suggest", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun suggest(
        @Parameter(
            description = "Prefix to match ingredient names against",
            example = "chi",
            required = true,
            `in` = ParameterIn.QUERY,
        )
        @RequestParam q: String,
    ): ResponseEntity<IngredientSuggestionsResponseDto> {
        val command = IngredientSuggestRequestDto(q).toCommand()
        return suggestIngredients.execute(command).fold(
            success = { ResponseEntity.ok(IngredientSuggestionsResponseDto.from(it)) },
            failure = { throw ApplicationException(it) },
        )
    }
}
