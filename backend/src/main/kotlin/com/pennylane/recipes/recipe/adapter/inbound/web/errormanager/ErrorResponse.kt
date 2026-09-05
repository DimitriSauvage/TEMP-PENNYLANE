package com.pennylane.recipes.recipe.adapter.inbound.web.errormanager

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import java.time.Instant

/** Standard error envelope shared by every error response. */
@Schema(description = "Standard error envelope returned by every API error response")
data class ErrorResponse(
    @field:Schema(description = "Server time of the failure", type = "string", format = "date-time", example = "2026-01-01T12:00:00Z")
    val timestamp: Instant,
    @field:Schema(description = "HTTP status code", example = "404")
    val status: Int,
    @field:Schema(description = "HTTP status reason phrase", example = "Not Found")
    val error: String,
    @field:Schema(description = "Request path that produced the error", example = "/api/recipes/42")
    val path: String,
    @field:Schema(description = "Stable machine-readable error code", example = "RECIPE_NOT_FOUND")
    val code: String,
    @field:Schema(description = "Human-readable description of the error", example = "Recipe not found")
    val message: String,
) {
    companion object {
        fun of(
            status: HttpStatus,
            path: String,
            code: String,
            message: String,
        ): ErrorResponse =
            ErrorResponse(
                timestamp = Instant.now(),
                status = status.value(),
                error = status.reasonPhrase,
                path = path,
                code = code,
                message = message,
            )
    }
}
