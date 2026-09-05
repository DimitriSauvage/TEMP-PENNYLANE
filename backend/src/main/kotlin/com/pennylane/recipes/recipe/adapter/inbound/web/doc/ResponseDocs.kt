package com.pennylane.recipes.recipe.adapter.inbound.web.doc

import com.pennylane.recipes.recipe.adapter.inbound.web.errormanager.ErrorResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

/**
 * Composed OpenAPI response-doc annotations. Each combines `@ApiResponse` with
 * the shared error envelope schema and examples, so controllers stay declarative
 * instead of repeating an `@ApiResponse` block per endpoint.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(responseCode = "200", description = "Successful response with the operation payload")
annotation class SuccessResponseDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request: a required parameter is missing or has a value that does not match the expected type",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "missingParameter",
                    summary = "A required query parameter is absent",
                    value = """
                        {
                          "timestamp": "2026-01-01T12:00:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "path": "/api/recipes/search",
                          "code": "MISSING_PARAMETER",
                          "message": "Required parameter 'ingredients' is missing"
                        }
                    """,
                ),
                ExampleObject(
                    name = "invalidParameter",
                    summary = "A parameter value does not match the expected type",
                    value = """
                        {
                          "timestamp": "2026-01-01T12:00:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "path": "/api/recipes/abc",
                          "code": "INVALID_PARAMETER",
                          "message": "Parameter 'id' received invalid value 'abc'; expected type Long"
                        }
                    """,
                ),
            ],
        ),
    ],
)
annotation class ValidationErrorResponseDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Not found: the requested resource does not exist",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "recipeNotFound",
                    summary = "No recipe exists for the given id",
                    value = """
                        {
                          "timestamp": "2026-01-01T12:00:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "path": "/api/recipes/999999",
                          "code": "RECIPE_NOT_FOUND",
                          "message": "Recipe not found"
                        }
                    """,
                ),
            ],
        ),
    ],
)
annotation class NotFoundResponseDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "422",
    description = "Unprocessable entity: the request is well-formed but violates a business rule",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "emptyIngredientList",
                    summary = "The search ran without any usable ingredient",
                    value = """
                        {
                          "timestamp": "2026-01-01T12:00:00Z",
                          "status": 422,
                          "error": "Unprocessable Entity",
                          "path": "/api/recipes/search",
                          "code": "EMPTY_INGREDIENT_LIST",
                          "message": "The ingredient list must contain at least one ingredient"
                        }
                    """,
                ),
            ],
        ),
    ],
)
annotation class UnprocessableResponseDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "500",
    description = "Internal server error: an unexpected failure occurred",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "internalError",
                    summary = "Unexpected server failure",
                    value = """
                        {
                          "timestamp": "2026-01-01T12:00:00Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "path": "/api/recipes/search",
                          "code": "INTERNAL_ERROR",
                          "message": "An unexpected error occurred"
                        }
                    """,
                ),
            ],
        ),
    ],
)
annotation class ServerErrorResponseDoc
