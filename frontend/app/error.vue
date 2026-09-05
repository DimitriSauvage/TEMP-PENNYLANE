<script setup lang="ts">
import type { NuxtError } from "#app";

// #region Props
defineProps<{ error: NuxtError }>();

// #region Head
useHead({ title: "Something went wrong — Recipe Finder" });
</script>

<template>
  <div
    class="flex min-h-dvh flex-col items-center justify-center gap-4 bg-surface-100 px-4 text-center"
  >
    <span
      class="flex h-16 w-16 items-center justify-center rounded-full bg-primary-100 text-primary-700"
      aria-hidden="true"
    >
      <i class="pi pi-exclamation-triangle text-3xl" />
    </span>
    <h1 class="text-2xl font-semibold text-surface-900">Something went wrong</h1>
    <p class="max-w-md text-surface-700">
      An unexpected error occurred while loading this page. Please try again.
    </p>
    <NuxtLink
      to="/"
      class="rounded-xl bg-primary px-5 py-2.5 text-sm font-medium text-primary-contrast shadow-sm transition-colors hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-primary"
    >
      Back to home
    </NuxtLink>
  </div>
</template>
