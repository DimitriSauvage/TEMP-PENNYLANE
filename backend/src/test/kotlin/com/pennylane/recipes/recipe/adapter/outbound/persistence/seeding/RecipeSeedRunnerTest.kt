package com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate

class RecipeSeedRunnerTest {
    private val jdbcTemplate = mockk<JdbcTemplate>()
    private val seeder = mockk<RecipeSeeder>()
    private val runner = RecipeSeedRunner(jdbcTemplate, seeder)

    @Test
    fun `empty catalog is seeded`() {
        every { jdbcTemplate.queryForObject(any(), Boolean::class.java) } returns false
        every { seeder.seed() } returns SeedCounts(1, 2, 3)

        runner.run(mockk())

        verify(exactly = 1) { seeder.seed() }
    }

    @Test
    fun `non-empty catalog is skipped`() {
        every { jdbcTemplate.queryForObject(any(), Boolean::class.java) } returns true

        runner.run(mockk())

        verify(exactly = 0) { seeder.seed() }
    }

    @Test
    fun `failed seed on empty catalog is rethrown`() {
        every { jdbcTemplate.queryForObject(any(), Boolean::class.java) } returns false
        every { seeder.seed() } throws IllegalStateException("download failed")

        assertThrows<IllegalStateException> { runner.run(mockk()) }
    }
}
