package com.pennylane.recipes.recipe.adapter.inbound.web.errormanager

import com.pennylane.recipes.recipe.application.error.UseCaseError
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.apache.catalina.connector.ClientAbortException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.HttpRequestMethodNotSupportedException

class ApplicationErrorHandlerTest {
    private val handler = ApplicationErrorHandler()
    private val request = mockk<HttpServletRequest> { every { requestURI } returns "/api/recipes/1" }

    @Test
    fun `sanitizeForLog replaces control characters so user input cannot forge log entries`() {
        assertThat(sanitizeForLog("va\nlue\rwith\u0000controls")).isEqualTo("va_lue_with_controls")
    }

    @Test
    fun `sanitizeForLog leaves clean values and null untouched`() {
        assertThat(sanitizeForLog("big")).isEqualTo("big")
        assertThat(sanitizeForLog(null)).isEqualTo("null")
    }

    @Test
    fun `every use case error group resolves to its locked http status`() {
        val cases: List<Pair<UseCaseError, HttpStatus>> =
            listOf(
                UseCaseError.NotFound.RecipeNotFound to HttpStatus.NOT_FOUND,
                UseCaseError.Unprocessable.EmptyIngredientList to HttpStatus.UNPROCESSABLE_ENTITY,
                UseCaseError.DomainValidation("SOME_CODE", "some message") to HttpStatus.BAD_REQUEST,
                UseCaseError.Internal("boom") to HttpStatus.INTERNAL_SERVER_ERROR,
            )
        cases.forEach { (error, expected) ->
            val response = handler.handleApplicationException(ApplicationException(error), request)
            assertThat(response.statusCode).`as`("status of %s", error::class.simpleName).isEqualTo(expected)
            assertThat(response.body!!.code).isEqualTo(error.code)
            assertThat(response.body!!.status).isEqualTo(expected.value())
        }
    }

    @Test
    fun `a client abort is rethrown so spring handles the disconnected connection`() {
        assertThrows<ClientAbortException> {
            handler.handleUnexpected(ClientAbortException("broken pipe"), request)
        }
    }

    @Test
    fun `spring error responses are rethrown so framework statuses like 405 stay theirs`() {
        assertThrows<HttpRequestMethodNotSupportedException> {
            handler.handleUnexpected(HttpRequestMethodNotSupportedException("POST"), request)
        }
    }
}
