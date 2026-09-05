package com.pennylane.recipes.recipe.adapter.outbound.persistence.adapter

import com.pennylane.recipes.recipe.domain.model.IngredientName
import com.pennylane.recipes.recipe.domain.model.Recipe
import com.pennylane.recipes.recipe.domain.model.RecipeId
import com.pennylane.recipes.recipe.domain.model.RecipeMatch
import com.pennylane.recipes.recipe.domain.model.RecipeSearchPage
import com.pennylane.recipes.recipe.domain.port.outbound.RecipeCatalogPort
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional

/**
 * SQL-side search: ranking, readiness filtering and pagination run in Postgres
 * (aggregate over recipe x ingredients, ORDER BY missing/matched/id, LIMIT/OFFSET),
 * so no code path loads the whole catalog. Ingredient names are stored already
 * canonicalized (the seeder normalizes through [IngredientName.of]), so `IN`-list
 * matching is exact string equality.
 */
@Transactional(readOnly = true)
open class RecipeCatalogAdapter(
    private val jdbc: NamedParameterJdbcTemplate,
) : RecipeCatalogPort {
    override fun searchByPantry(
        pantry: Set<IngredientName>,
        readyToCookOnly: Boolean,
        page: Int,
        size: Int,
    ): RecipeSearchPage {
        val params =
            MapSqlParameterSource()
                .addValue("pantry", pantry.map { it.value })
                .addValue("limit", size)
                .addValue("offset", page * size)

        // id -> (name, imageUrl), insertion order = rank order
        val pageRecipes = LinkedHashMap<Long, Pair<String, String?>>()
        jdbc.query(
            pageQuery(readyToCookOnly),
            params,
        ) { rs, _ -> pageRecipes[rs.getLong("id")] = rs.getString("name") to rs.getString("image_url") }

        val totalElements = (jdbc.queryForObject(countQuery(readyToCookOnly), params, Long::class.java) ?: 0L).toInt()

        return RecipeSearchPage(
            totalElements = totalElements,
            matches = if (pageRecipes.isEmpty()) emptyList() else matchesFor(pageRecipes, pantry),
        )
    }

    // One aggregate row per recipe: total vs pantry-matched ingredient counts.
    // LEFT JOIN keeps zero-ingredient recipes visible, like the old catalog scan did.
    private val rankedSubquery =
        """
        SELECT r.id, r.name, r.image_url,
               COUNT(i.id) AS total_count,
               COUNT(i.id) FILTER (WHERE i.name IN (:pantry)) AS matched_count
        FROM recipe r
        LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
        LEFT JOIN ingredient i ON i.id = ri.ingredient_id
        GROUP BY r.id, r.name, r.image_url
        """.trimIndent()

    private fun pageQuery(readyToCookOnly: Boolean): String =
        """
        SELECT id, name, image_url
        FROM ($rankedSubquery) ranked
        ${if (readyToCookOnly) "WHERE matched_count = total_count" else ""}
        ORDER BY total_count - matched_count, matched_count DESC, id
        LIMIT :limit OFFSET :offset
        """.trimIndent()

    private fun countQuery(readyToCookOnly: Boolean): String =
        """
        SELECT COUNT(*)
        FROM ($rankedSubquery) ranked
        ${if (readyToCookOnly) "WHERE matched_count = total_count" else ""}
        """.trimIndent()

    // Ingredient names for just the page's recipes; matched/missing split in Kotlin.
    private fun matchesFor(
        pageRecipes: Map<Long, Pair<String, String?>>,
        pantry: Set<IngredientName>,
    ): List<RecipeMatch> {
        val namesByRecipe = HashMap<Long, MutableList<String>>()
        jdbc.query(
            """
            SELECT ri.recipe_id, i.name
            FROM recipe_ingredient ri
            JOIN ingredient i ON i.id = ri.ingredient_id
            WHERE ri.recipe_id IN (:recipeIds)
            ORDER BY i.name
            """.trimIndent(),
            MapSqlParameterSource("recipeIds", pageRecipes.keys),
        ) { rs, _ -> namesByRecipe.getOrPut(rs.getLong("recipe_id")) { mutableListOf() }.add(rs.getString("name")) }

        return pageRecipes.map { (id, meta) ->
            val recipe =
                Recipe.rehydrate(
                    id = RecipeId(id),
                    name = meta.first,
                    imageUrl = meta.second,
                    ingredients = namesByRecipe[id].orEmpty(),
                    instructions = emptyList(),
                )
            RecipeMatch(
                recipe = recipe,
                matched = recipe.ingredients.filter { it in pantry },
                missing = recipe.ingredients.filterNot { it in pantry },
            )
        }
    }
}
