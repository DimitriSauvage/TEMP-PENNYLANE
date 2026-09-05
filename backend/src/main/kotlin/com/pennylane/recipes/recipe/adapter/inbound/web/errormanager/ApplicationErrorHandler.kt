package com.pennylane.recipes.recipe.adapter.inbound.web.errormanager

import com.pennylane.recipes.recipe.application.error.UseCaseError
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.apache.catalina.connector.ClientAbortException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * Maps every typed application error, every malformed request parameter and any
 * unexpected exception to the shared [ErrorResponse] envelope. The `when` over
 * [UseCaseError] groups is exhaustive, so a new group forces an explicit HTTP
 * mapping here.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApplicationErrorHandler {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(
        ex: ApplicationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status = resolveStatus(ex.error)
        logError(status, ex.error)
        return ResponseEntity
            .status(status)
            .body(ErrorResponse.of(status, request.requestURI, ex.error.code, ex.error.message))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        log.warn { "400 MISSING_PARAMETER on ${request.requestURI}: '${ex.parameterName}' is missing" }
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse.of(
                    HttpStatus.BAD_REQUEST,
                    request.requestURI,
                    "MISSING_PARAMETER",
                    "Required parameter '${ex.parameterName}' is missing",
                ),
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val expectedType = ex.requiredType?.simpleName ?: "unknown"
        log.warn { "400 INVALID_PARAMETER on ${request.requestURI}: '${ex.name}' = '${sanitizeForLog(ex.value)}'" }
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse.of(
                    HttpStatus.BAD_REQUEST,
                    request.requestURI,
                    "INVALID_PARAMETER",
                    "Parameter '${ex.name}' received invalid value '${ex.value}'; expected type $expectedType",
                ),
            )
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleMethodValidation(
        ex: HandlerMethodValidationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val violations =
            ex.parameterValidationResults.joinToString("; ") { result ->
                "'${result.methodParameter.parameterName}' " +
                    result.resolvableErrors.joinToString(", ") { it.defaultMessage ?: "is invalid" }
            }
        log.warn { "400 INVALID_PARAMETER on ${request.requestURI}: $violations" }
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponse.of(
                    HttpStatus.BAD_REQUEST,
                    request.requestURI,
                    "INVALID_PARAMETER",
                    "Parameter validation failed: $violations",
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        // Rethrowing makes the resolver chain fall through to Spring's default
        // handling, keeping 404/405/406/415. Fully qualified: this package has its own ErrorResponse.
        if (ex is org.springframework.web.ErrorResponse) throw ex
        // A client that disconnected mid-response is expected noise, not a server failure.
        if (ex is ClientAbortException) {
            log.debug { "client aborted the connection on ${request.requestURI}: ${ex.message}" }
            throw ex
        }
        log.error { "500 INTERNAL_ERROR on ${request.requestURI}: ${ex.javaClass.simpleName}: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request.requestURI,
                    "INTERNAL_ERROR",
                    "An unexpected error occurred",
                ),
            )
    }

    private fun resolveStatus(error: UseCaseError): HttpStatus =
        when (error) {
            is UseCaseError.NotFound -> HttpStatus.NOT_FOUND
            is UseCaseError.Unprocessable -> HttpStatus.UNPROCESSABLE_ENTITY
            is UseCaseError.DomainValidation -> HttpStatus.BAD_REQUEST
            is UseCaseError.Internal -> HttpStatus.INTERNAL_SERVER_ERROR
        }

    private fun logError(
        status: HttpStatus,
        error: UseCaseError,
    ) {
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error { "500 ${error.code}: ${error.message}" }
        } else {
            log.warn { "${status.value()} ${error.code}: ${error.message}" }
        }
    }
}

// Log-injection guard (OWASP A09-adjacent): control characters in a user-supplied value could
// forge log entries; the JSON response body is Jackson-encoded and needs no sanitizing.
private val controlCharacters = Regex("\\p{Cntrl}")

internal fun sanitizeForLog(value: Any?): String = value.toString().replace(controlCharacters, "_")
