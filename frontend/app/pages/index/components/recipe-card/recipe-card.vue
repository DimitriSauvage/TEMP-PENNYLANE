<script setup lang="ts">
import { formatMatchStatus } from "./recipe-card.utils";
import type { RecipeCardProps } from "./recipe-card.types";

// #region Props
const props = defineProps<RecipeCardProps>();

// #region Computed
const status = computed(() => formatMatchStatus(props.recipe));
const imageFailed = ref(false);
</script>

<template>
  <NuxtLink
    :to="`/recipes/${recipe.id}`"
    class="group flex flex-col overflow-hidden rounded-xl bg-surface-0 shadow-sm ring-1 ring-surface-200 transition-all duration-200 hover:-translate-y-1 hover:shadow-lg focus-visible:outline-2 focus-visible:outline-primary"
  >
    <div class="relative overflow-hidden">
      <img
        v-if="recipe.imageUrl && !imageFailed"
        :src="recipe.imageUrl"
        :alt="recipe.name"
        loading="lazy"
        decoding="async"
        class="h-44 w-full object-cover transition-transform duration-300 group-hover:scale-105"
        @error="imageFailed = true"
      />
      <div
        v-else
        class="flex h-44 w-full items-center justify-center bg-surface-100"
        aria-hidden="true"
      >
        <i class="pi pi-image text-3xl text-surface-400" />
      </div>
      <span v-if="recipe.readyToCook" class="absolute left-3 top-3">
        <Tag value="Ready to cook" severity="success" />
      </span>
    </div>
    <div class="flex flex-1 flex-col gap-2 p-4">
      <h3
        class="line-clamp-2 text-base font-semibold text-surface-900 transition-colors group-hover:text-primary"
      >
        {{ recipe.name }}
      </h3>
      <p v-if="!recipe.readyToCook" class="mt-auto flex items-start gap-1.5 text-sm text-surface-600">
        <i class="pi pi-shopping-bag mt-0.5 text-primary-500" aria-hidden="true" />
        <span>{{ status }}</span>
      </p>
    </div>
  </NuxtLink>
</template>
