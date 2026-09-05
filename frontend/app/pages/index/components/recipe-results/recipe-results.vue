<script setup lang="ts">
/**
 * Results section under the search filters: heading, empty-ingredients
 * prompt, loading skeleton, error, no-match states, the recipe card grid
 * and pagination. All query state arrives as props; the page owns fetching.
 */
import RecipeCard from "~/pages/index/components/recipe-card/recipe-card.vue";
import { SEARCH_PAGE_SIZE } from "~/composables/use-recipe-search";
import type { RecipeResultsEmits, RecipeResultsProps } from "./recipe-results.types";

// #region Props
const props = defineProps<RecipeResultsProps>();

// #region Emits
const emit = defineEmits<RecipeResultsEmits>();

// #region Computed
const totalPages = computed(() => Math.ceil(props.totalElements / SEARCH_PAGE_SIZE));

// #region Handlers
function onPageChange(event: { page: number }): void {
  emit("page-change", event.page);
}

function onRetry(): void {
  emit("retry");
}
</script>

<template>
  <section class="mt-10 flex flex-col gap-5" aria-labelledby="results-heading">
    <div class="flex flex-col gap-1">
      <h2 id="results-heading" class="text-lg font-semibold text-surface-900">Recipes</h2>
      <p v-if="results.length > 0" class="text-sm text-muted">
        {{ totalElements }} recipes, ranked by what you already have
      </p>
    </div>

    <div
      v-if="!hasIngredients"
      class="flex flex-col items-center gap-4 rounded-2xl border border-dashed border-surface-300 bg-surface-0 px-6 py-14 text-center"
    >
      <span
        class="flex h-14 w-14 items-center justify-center rounded-full bg-primary-100 text-primary-700"
        aria-hidden="true"
      >
        <i class="pi pi-sparkles text-2xl" />
      </span>
      <p class="max-w-md text-surface-700">
        Add what you have at home to find recipes you can cook.
      </p>
    </div>

    <div
      v-else-if="isPending"
      class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3"
      aria-busy="true"
    >
      <div
        v-for="n in SEARCH_PAGE_SIZE"
        :key="n"
        class="overflow-hidden rounded-xl bg-surface-0 shadow-sm ring-1 ring-surface-200"
      >
        <Skeleton class="block h-44 w-full rounded-none" />
        <div class="flex flex-col gap-2 p-4">
          <Skeleton class="h-4 w-3/4 rounded" />
          <Skeleton class="h-4 w-1/2 rounded" />
        </div>
      </div>
    </div>

    <div
      v-else-if="isError"
      role="alert"
      class="flex flex-col items-start gap-3 rounded-xl border border-danger-100 bg-danger-50 p-5"
    >
      <p class="flex items-center gap-2 font-medium text-surface-800">
        <i class="pi pi-exclamation-triangle text-danger" aria-hidden="true" />
        {{ error?.message || "Something went wrong while loading recipes." }}
      </p>
      <Button
        label="Try again"
        icon="pi pi-refresh"
        severity="secondary"
        size="small"
        @click="onRetry"
      />
    </div>

    <template v-else-if="results.length === 0">
      <div
        class="flex flex-col items-center gap-4 rounded-2xl border border-dashed border-surface-300 bg-surface-0 px-6 py-12 text-center"
      >
        <span
          class="flex h-14 w-14 items-center justify-center rounded-full bg-primary-100 text-primary-700"
          aria-hidden="true"
        >
          <i class="pi pi-face-smile text-2xl" />
        </span>
        <p v-if="readyToCookOnly" class="max-w-md text-surface-700">
          No recipe uses ALL your ingredients — try turning off 'Ready to cook only'.
        </p>
        <template v-else>
          <p class="max-w-md text-surface-700">No recipe matches these ingredients.</p>
          <p class="max-w-md text-sm text-muted">
            Try removing some ingredients, or add a few more of what you have.
          </p>
        </template>
      </div>
    </template>

    <template v-else>
      <div
        class="grid grid-cols-1 gap-5 transition-opacity duration-150 sm:grid-cols-2 lg:grid-cols-3"
        :class="{ 'pointer-events-none opacity-60': isFetching && !isPending }"
        :aria-busy="isFetching"
      >
        <RecipeCard v-for="recipe in results" :key="recipe.id" :recipe="recipe" />
      </div>
      <Paginator
        v-if="totalPages > 1"
        :rows="SEARCH_PAGE_SIZE"
        :total-records="totalElements"
        :first="page * SEARCH_PAGE_SIZE"
        @page="onPageChange"
      />
    </template>
  </section>
</template>
