package com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Seeds the catalog at startup when (and only when) it is empty. There is no
 * on/off switch: an empty catalog is always seeded from `app.seed.url`. A
 * failed seed on an empty catalog aborts boot — the app never silently starts
 * serving an empty catalog.
 */
class RecipeSeedRunner(
    private val jdbcTemplate: JdbcTemplate,
    private val seeder: RecipeSeeder,
) : ApplicationRunner {
    private val log = KotlinLogging.logger {}

    override fun run(args: ApplicationArguments) {
        val catalogHasRecipes =
            jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM recipe)", Boolean::class.java) ?: false
        if (catalogHasRecipes) {
            log.info { "Recipe catalog already seeded, skipping" }
            return
        }
        try {
            seeder.seed()
        } catch (ex: Exception) {
            log.error(ex) { "Seeding failed and the catalog is empty - refusing to boot" }
            throw ex
        }
    }
}
