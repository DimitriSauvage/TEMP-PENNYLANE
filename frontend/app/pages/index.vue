<script setup lang="ts">
import RecipeResults from "~/pages/index/components/recipe-results/recipe-results.vue";
import SearchFilters from "~/pages/index/components/search-filters/search-filters.vue";

// #region Composables
const { ingredients } = useIngredientList();
// Declared before the search wiring: vue-query evaluates the filters computed
// eagerly during setup, so a later declaration would crash (TDZ).
const readyToCookOnly = ref(false);
const { query, page, setPage } = useRecipeSearch(
  computed(() => ({
    ingredients: ingredients.value,
    readyToCookOnly: readyToCookOnly.value,
  })),
);
const { data, isPending, isError, isFetching, error, refetch } = query;

// #region Head
useHead({ title: "Search recipes — Recipe Finder" });

// #region Computed
const results = computed(() => data.value?.items ?? []);
const totalElements = computed(() => data.value?.totalElements ?? 0);

// #region Watchers
// Watch the list length, not the ref: useIngredientList mutates via push(),
// which a shallow ref watch would miss — adding an ingredient must reset the page.
watch([() => ingredients.value.length, readyToCookOnly], () => setPage(0));

// #region Handlers
function onPageChange(next: number): void {
  setPage(next);
}

function onRetry(): void {
  void refetch();
}
</script>

<template>
  <div class="flex flex-col">
    <SearchFilters v-model:ready-to-cook-only="readyToCookOnly" />
    <RecipeResults
      :results="results"
      :total-elements="totalElements"
      :is-pending="isPending"
      :is-error="isError"
      :is-fetching="isFetching"
      :error="error"
      :has-ingredients="ingredients.length > 0"
      :ready-to-cook-only="readyToCookOnly"
      :page="page"
      @retry="onRetry"
      @page-change="onPageChange"
    />
  </div>
</template>
