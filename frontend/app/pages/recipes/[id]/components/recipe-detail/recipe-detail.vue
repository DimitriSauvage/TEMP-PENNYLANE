<script setup lang="ts">
/**
 * Body of the recipe detail page: name, image, the ingredients list with
 * missing markers, and the numbered preparation steps.
 */
import RecipeIngredients from "~/pages/recipes/[id]/components/recipe-ingredients/recipe-ingredients.vue";
import type { RecipeDetailProps } from "./recipe-detail.types";

// #region Props
defineProps<RecipeDetailProps>();
const imageFailed = ref(false);
</script>

<template>
  <article class="flex flex-col gap-6">
    <h2 class="text-3xl font-bold text-surface-900">{{ recipe.name }}</h2>
    <img
      v-if="recipe.imageUrl && !imageFailed"
      :src="recipe.imageUrl"
      :alt="recipe.name"
      class="h-80 w-full rounded-2xl object-cover shadow-md md:h-96"
      @error="imageFailed = true"
    />
    <div
      v-else
      class="flex h-80 w-full items-center justify-center rounded-2xl bg-surface-100 shadow-sm md:h-96"
      aria-hidden="true"
    >
      <i class="pi pi-image text-5xl text-surface-400" />
    </div>
    <div class="grid grid-cols-1 gap-6 md:grid-cols-2">
      <section
        class="flex flex-col gap-4 rounded-2xl border border-surface-200 bg-surface-0 p-5 shadow-sm"
        aria-labelledby="ingredients-heading"
      >
        <h3
          id="ingredients-heading"
          class="flex items-center gap-2 text-lg font-semibold text-surface-900"
        >
          <i class="pi pi-list-check text-primary-600" aria-hidden="true" />
          Ingredients
        </h3>
        <RecipeIngredients :ingredients="recipe.ingredients" :user-ingredients="userIngredients" />
      </section>
      <section
        class="flex flex-col gap-4 rounded-2xl border border-surface-200 bg-surface-0 p-5 shadow-sm"
        aria-labelledby="instructions-heading"
      >
        <h3
          id="instructions-heading"
          class="flex items-center gap-2 text-lg font-semibold text-surface-900"
        >
          <i class="pi pi-receipt text-primary-600" aria-hidden="true" />
          Preparation
        </h3>
        <p v-if="recipe.instructions.length === 0" class="text-base text-surface-700">
          Under development :)
        </p>
        <ol
          v-else
          class="flex list-decimal flex-col gap-3 pl-5 text-base marker:font-semibold marker:text-primary-700"
        >
          <li v-for="(step, index) in recipe.instructions" :key="index" class="text-surface-700">
            {{ step }}
          </li>
        </ol>
      </section>
    </div>
  </article>
</template>
