<script setup lang="ts">
/**
 * Search filters above the results grid: the ingredient picker plus the
 * "Ready to cook only" toggle. Owns the toggle's useId-based label wiring.
 */
import IngredientPicker from "~/pages/index/components/ingredient-picker/ingredient-picker.vue";
import type { SearchFiltersEmits, SearchFiltersProps } from "./search-filters.types";

// #region Props
const props = defineProps<SearchFiltersProps>();

// #region Emits
const emit = defineEmits<SearchFiltersEmits>();

// #region Composables
const toggleId = useId();

// #region Computed
const toggleValue = computed({
  get: (): boolean => props.readyToCookOnly,
  set: (value: boolean) => emit("update:readyToCookOnly", value),
});
</script>

<template>
  <search
    class="flex flex-col gap-5 rounded-2xl border border-surface-200 bg-surface-0 p-5 shadow-sm sm:p-6"
    aria-label="Find recipes with your ingredients"
  >
    <IngredientPicker />
    <div class="flex items-center gap-3 border-t border-surface-100 pt-4">
      <ToggleSwitch v-model="toggleValue" :input-id="`${toggleId}-toggle`" />
      <label :for="`${toggleId}-toggle`" class="text-sm font-medium text-surface-700">
        Ready to cook only
      </label>
    </div>
  </search>
</template>
