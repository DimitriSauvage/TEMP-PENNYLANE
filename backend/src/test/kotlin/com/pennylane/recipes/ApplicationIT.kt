package com.pennylane.recipes

import com.pennylane.recipes.recipe.adapter.outbound.persistence.AbstractPostgresIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
class ApplicationIT(
    private val jdbcTemplate: JdbcTemplate,
    private val mockMvc: MockMvc,
) : AbstractPostgresIT() {
    @Test
    fun `context loads, Liquibase ran and database wiring works`() {
        val one = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
        assertThat(one).isEqualTo(1)

        val liquibaseTable =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE lower(table_name) = 'databasechangelog'",
                Int::class.java,
            )
        assertThat(liquibaseTable).isEqualTo(1)
    }

    @Test
    fun `actuator health reports UP against the real database`() {
        mockMvc.get("/actuator/health").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
        }
    }
}
