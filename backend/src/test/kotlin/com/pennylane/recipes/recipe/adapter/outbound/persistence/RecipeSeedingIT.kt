package com.pennylane.recipes.recipe.adapter.outbound.persistence

import com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding.RecipeSeeder
import com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding.SeedCounts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Seeding behavior against a real Postgres schema: counts, ingredient-name
 * dedup, quantity stripping and idempotency. Runs the seeder twice against
 * the same database and asserts identical outcomes.
 */
@SpringBootTest(properties = ["app.seed.url=classpath:seeding/seeding-fixture.json"])
class RecipeSeedingIT(
    private val seeder: RecipeSeeder,
    private val jdbcTemplate: JdbcTemplate,
) : AbstractPostgresIT() {
    @Test
    fun `seed is idempotent and dedups normalized ingredient names`() {
        val first = seeder.seed()
        assertCounts(first)
        val second = seeder.seed()
        assertThat(second).isEqualTo(first)
        assertCounts(second)
    }

    @Test
    fun `seed truncates over-length title and image to the column widths`() {
        seeder.seed()

        val name = jdbcTemplate.queryForObject("SELECT name FROM recipe WHERE id = 3", String::class.java)
        val imageUrl = jdbcTemplate.queryForObject("SELECT image_url FROM recipe WHERE id = 3", String::class.java)
        assertThat(name).hasSize(255).startsWith("Truncation Target")
        assertThat(imageUrl).hasSize(500).startsWith("http://img.example/overlong/")
    }

    @Test
    fun `seed unwraps dead image proxy urls and drops the placeholder`() {
        seeder.seed()

        val imageUrl = jdbcTemplate.queryForObject("SELECT image_url FROM recipe WHERE id = 2", String::class.java)
        assertThat(imageUrl).isEqualTo("https://images.media-allrecipes.com/userphotos/1234567.jpg")

        val placeholder = jdbcTemplate.queryForObject("SELECT image_url FROM recipe WHERE id = 4", String::class.java)
        assertThat(placeholder).isNull()
    }

    private fun assertCounts(counts: SeedCounts) {
        assertThat(counts.recipes).isEqualTo(4)
        assertThat(counts.ingredients).isEqualTo(6)
        assertThat(counts.recipeIngredients).isEqualTo(8)

        // "Chicken Breasts" (recipe one) and "chicken breast" (recipe two) fold to ONE row
        val chickenRows =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ingredient WHERE name = 'chicken breast'",
                Int::class.java,
            )
        assertThat(chickenRows).isEqualTo(1)

        // raw "1 cup all-purpose flour" becomes ingredient "all purpose flour";
        // raw "1 ½ cups warm water" (unicode fraction) becomes ingredient "warm water"
        val storedNames =
            jdbcTemplate.queryForList("SELECT name FROM ingredient ORDER BY name", String::class.java)
        assertThat(storedNames).containsExactly(
            "all purpose flour",
            "chicken breast",
            "garlic",
            "rice",
            "salt",
            "warm water",
        )

        val recipeIds = jdbcTemplate.queryForList("SELECT id FROM recipe ORDER BY id", Long::class.java)
        assertThat(recipeIds).containsExactly(1L, 2L, 3L, 4L)
    }
}
