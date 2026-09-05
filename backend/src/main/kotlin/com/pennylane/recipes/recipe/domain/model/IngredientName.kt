package com.pennylane.recipes.recipe.domain.model

/**
 * Normalized ingredient name. Two raw names normalize to the same [IngredientName]
 * when they denote the same ingredient: case, whitespace, punctuation and naive
 * plural differences are erased, e.g. "Chicken Breasts" equals "chicken breast".
 */
@JvmInline
value class IngredientName(
    val value: String,
) {
    /**
     * A normalized name that still has no content is unusable — e.g. the raw
     * input was only punctuation, which normalization strips away.
     */
    val isValid: Boolean
        get() = value.isNotBlank()

    companion object {
        // Regex to remove the characters not accepted in a word
        private val nonWord = Regex("[^\\p{L}\\p{N}]+")

        fun of(raw: String): IngredientName {
            val normalized = raw.lowercase().replace(nonWord, " ").trim()
            val singular = normalized.split(" ").joinToString(" ") { singular(it) }
            return IngredientName(singular)
        }

        // naive singularization (drop trailing "s"); swap in a stemming
        // library if the catalog ever shows plural pairs it fails to fold.
        private fun singular(word: String): String = if (word.length > 3 && word.endsWith("s")) word.dropLast(1) else word
    }
}
