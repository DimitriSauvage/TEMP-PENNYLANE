<script setup lang="ts">
// #region Composables
const { ingredients, add, remove, clear } = useIngredientList();
const { suggestions, isSearching, search } = useIngredientSuggestions();
const id = useId();

// #region State
const input = ref("");

// #region Computed
const availableSuggestions = computed(() =>
  suggestions.value.filter(
    (name) =>
      !ingredients.value.some((ingredient) => ingredient.toLowerCase() === name.toLowerCase()),
  ),
);

// #region Handlers
function onSearch(event: { query: string }): void {
  search(event.query);
}

function onSelect(event: { value: string }): void {
  if (add(event.value)) input.value = "";
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <label :for="`${id}-input`" class="text-sm font-semibold text-surface-800">
      Add an ingredient
    </label>
    <AutoComplete
      v-model="input"
      :input-id="`${id}-input`"
      :suggestions="availableSuggestions"
      :loading="isSearching"
      :delay="0"
      force-selection
      size="large"
      placeholder="Start typing, e.g. chicken"
      class="w-full"
      input-class="w-full"
      @complete="onSearch"
      @item-select="onSelect"
    />
    <p class="flex items-start gap-1.5 text-sm text-muted">
      <i class="pi pi-info-circle mt-0.5 text-primary-500" aria-hidden="true" />
      <span
        >Salt, pepper, water and olive oil are assumed in your kitchen — no need to add them.</span
      >
    </p>
    <template v-if="ingredients.length > 0">
      <h2 class="flex items-center gap-1.5 text-sm font-semibold text-surface-800">
        <i class="pi pi-shopping-bag text-primary-600" aria-hidden="true" />
        Your ingredients
      </h2>
      <ul class="flex flex-wrap gap-2">
        <li
          v-for="ingredient in ingredients"
          :key="ingredient"
          class="inline-flex items-center gap-1.5 rounded-full border border-primary-100 bg-primary-50 px-3 py-1 text-sm font-medium text-primary-800"
        >
          <span>{{ ingredient }}</span>
          <button
            type="button"
            class="rounded-full text-primary-500 transition-colors hover:text-primary-800 focus-visible:outline-2 focus-visible:outline-primary"
            :aria-label="`Remove ${ingredient}`"
            @click="remove(ingredient)"
          >
            <i class="pi pi-times text-xs" aria-hidden="true" />
          </button>
        </li>
      </ul>
      <div>
        <Button label="Clear all" severity="secondary" size="small" variant="text" @click="clear" />
      </div>
    </template>
  </div>
</template>
