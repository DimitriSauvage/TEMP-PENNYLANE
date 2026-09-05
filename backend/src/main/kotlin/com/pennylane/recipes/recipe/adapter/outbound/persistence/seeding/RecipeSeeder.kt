package com.pennylane.recipes.recipe.adapter.outbound.persistence.seeding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pennylane.recipes.recipe.domain.model.IngredientName
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.io.InputStream
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.zip.GZIPInputStream

/** Row counts produced by one seeding run; two runs of the same file yield equal values. */
data class SeedCounts(
    val recipes: Int,
    val ingredients: Int,
    val recipeIngredients: Int,
)

/**
 * Loads the recipes catalog into the database. Idempotent: truncates the
 * catalog tables then re-inserts everything within a single transaction, so re-running
 * with the same source always produces the same counts and ids (recipes get enumerated
 * ids 1..n). Ingredient names are canonicalized through the domain ([IngredientName.of])
 * before insertion, which folds e.g. "Chicken Breasts" and "chicken breast" into one row.
 *
 * The seed source ([seedUrl]) is an http(s) URL (downloaded, gunzipped when it ends in
 * `.gz`), a `classpath:`/`file:` location, or a bare filesystem path.
 */
class RecipeSeeder(
    private val jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader = DefaultResourceLoader(),
    private val seedUrl: String,
) {
    private val log = KotlinLogging.logger {}

    private val httpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

    private data class RawRecipe(
        val title: String? = null,
        val ingredients: List<String> = emptyList(),
        val image: String? = null,
    )

    fun seed(): SeedCounts =
        requireNotNull(
            transactionTemplate.execute {
                val rawRecipes = readRecipes()
                jdbcTemplate.execute("TRUNCATE TABLE recipe_ingredient, ingredient, recipe")

                val ingredientIdByName = LinkedHashMap<String, Long>()
                val recipeRows = ArrayList<Array<Any?>>(rawRecipes.size)
                val joinRows = ArrayList<Array<Any?>>()

                rawRecipes.forEachIndexed { index, raw ->
                    val title = raw.title?.trim().orEmpty()
                    val names = raw.ingredients.mapNotNull(::canonicalIngredient).distinct()
                    if (title.isEmpty() || names.isEmpty()) return@forEachIndexed

                    val recipeId = index + 1L
                    // recipe.name is varchar(255) and image_url varchar(500) (001-create-recipe-tables.xml):
                    // truncate so one over-length dataset row cannot abort boot at insert time.
                    recipeRows.add(arrayOf(recipeId, title.take(255), normalizeImageUrl(raw.image)?.take(500)))
                    names.forEach { name ->
                        val ingredientId = ingredientIdByName.getOrPut(name) { ingredientIdByName.size + 1L }
                        joinRows.add(arrayOf(recipeId, ingredientId))
                    }
                }

                jdbcTemplate.batchUpdate("INSERT INTO recipe (id, name, image_url) VALUES (?, ?, ?)", recipeRows)
                jdbcTemplate.batchUpdate(
                    "INSERT INTO ingredient (id, name) VALUES (?, ?)",
                    ingredientIdByName.map { (name, id) -> arrayOf<Any?>(id, name) },
                )
                jdbcTemplate.batchUpdate(
                    "INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES (?, ?)",
                    joinRows,
                )

                SeedCounts(recipeRows.size, ingredientIdByName.size, joinRows.size).also { counts ->
                    log.info {
                        "Seeded ${counts.recipes} recipes, ${counts.ingredients} ingredients, " +
                            "${counts.recipeIngredients} recipe-ingredient links"
                    }
                }
            },
        )

    private fun readRecipes(): List<RawRecipe> =
        openSeedStream().use { raw ->
            val stream = if (seedUrl.endsWith(".gz")) GZIPInputStream(raw) else raw
            objectMapper.readValue(stream)
        }

    private fun openSeedStream(): InputStream =
        when {
            seedUrl.startsWith("http://") || seedUrl.startsWith("https://") -> download()
            seedUrl.startsWith("classpath:") || seedUrl.startsWith("file:") ->
                resourceLoader.getResource(seedUrl).inputStream
            else -> FileSystemResource(seedUrl).inputStream
        }

    private fun download(): InputStream {
        val request =
            HttpRequest
                .newBuilder(URI.create(seedUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != 200) {
            response.body().close()
            throw IllegalStateException("Seed download failed: HTTP ${response.statusCode()} for $seedUrl")
        }
        return response.body()
    }

    private fun canonicalIngredient(raw: String): String? {
        val stripped = stripQuantityAndUnits(raw)
        if (stripped.isBlank()) return null
        return IngredientName.of(stripped).takeIf { it.isValid }?.value
    }

    /**
     * The dataset wraps image URLs in the imagesvc.meredithcorp.io resize proxy,
     * which was shut down and now answers HTTP 400. Unwrap the inner `url=`
     * parameter — the direct images.media-allrecipes.com URLs still work. The
     * generic og-default placeholder (now HTTP 402) is treated as no image: the
     * UI shows its own placeholder instead of a broken one.
     */
    private fun normalizeImageUrl(url: String?): String? {
        if (url == null || url == OG_DEFAULT_IMAGE) return null
        if (!url.startsWith(DEAD_IMAGE_PROXY)) return url
        val inner =
            URI(url)
                .query
                ?.split('&')
                ?.firstOrNull { it.startsWith("url=") }
                ?.removePrefix("url=")
                ?: return null
        return URLDecoder.decode(inner, Charsets.UTF_8).takeIf { it.startsWith("https://") }
    }

    private fun stripQuantityAndUnits(raw: String): String {
        var rest = raw.trim()
        while (true) {
            val next = stripUnit(stripQuantity(rest))
            if (next == rest || next.isEmpty()) return next
            rest = next
        }
    }

    // Digits, decimal separators, fractions ("1/2", unicode "½") and the spaces between them.
    private val quantity = Regex("^[\\d\\s.,/\\u00BC-\\u00BE\\u2150-\\u215E]+")

    // Units only ever stripped right after a leading quantity ("1 cup of flour" -> "flour",
    // "pinch of salt" -> "salt"); words like "large" or "canned" are protected by \b.
    private val unitWord = Regex("^(?:$UNITS)\\b[.,]?\\s*", RegexOption.IGNORE_CASE)

    private fun stripQuantity(s: String): String = quantity.find(s)?.let { s.substring(it.value.length).trim() } ?: s

    private fun stripUnit(s: String): String = unitWord.find(s)?.let { s.substring(it.value.length).trim() } ?: s

    private companion object {
        const val UNITS =
            "cups?|tablespoons?|teaspoons?|tbsps?|tsp|ounces?|ozs?|oz|pounds?|lbs?|lb|grams?|g|kgs?|kg|" +
                "milliliters?|millilitres?|mls?|ml|liters?|litres?|ls?|l|cloves?|slices?|pinch(?:es)?|dash(?:es)?|" +
                "quarts?|pints?|cans?|packages?|pkgs?|pkg|sprigs?|leaf|leaves|sticks?|bunch(?:es)?|heads?|of"

        const val DEAD_IMAGE_PROXY = "https://imagesvc.meredithcorp.io/"
        const val OG_DEFAULT_IMAGE = "https://www.allrecipes.com/img/misc/og-default.png"
    }
}
