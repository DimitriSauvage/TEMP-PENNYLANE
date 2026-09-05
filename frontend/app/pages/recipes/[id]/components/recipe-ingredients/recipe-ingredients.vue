<script setup lang="ts">
import { computeMissingIngredients } from "./recipe-ingredients.utils";
import type { RecipeIngredientsProps } from "./recipe-ingredients.types";

// #region Props
const props = defineProps<RecipeIngredientsProps>();

// #region Computed
const missing = computed(
  () => new Set(computeMissingIngredients(props.ingredients, props.userIngredients)),
);
</script>

<template>
  <ul class="flex flex-col gap-2.5">
    <li
      v-for="ingredient in ingredients"
      :key="ingredient"
      class="flex items-start gap-2 text-base text-surface-800"
    >
      <i
        class="pi mt-1 text-sm"
        :class="
          missing.has(ingredient) ? 'pi-times-circle text-danger' : 'pi-check-circle text-success'
        "
        aria-hidden="true"
      />
      <span>
        {{ ingredient }}
        <span v-if="missing.has(ingredient)" class="font-semibold text-danger">— missing</span>
      </span>
    </li>
  </ul>
</template>
