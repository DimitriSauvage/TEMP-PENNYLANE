<script setup lang="ts">
import RecipeDetail from "~/pages/recipes/[id]/components/recipe-detail/recipe-detail.vue";

// #region Composables
const route = useRoute();
const router = useRouter();
const { ingredients } = useIngredientList();
const recipeId = computed(() => String(route.params["id"]));
const { data: recipe, isPending, isError, error, refetch } = useRecipeDetail(recipeId);
// A disabled query reports isPending, so a non-numeric id must be routed to
// the error state explicitly or the skeleton would show forever.
const invalidId = computed(() => !Number.isInteger(Number(recipeId.value)));

// #region Head
useHead({
  title: computed(() => (recipe.value ? `${recipe.value.name} — Recipe Finder` : "Recipe Finder")),
});

// #region Handlers
function goBack(): void {
  if (window.history.length > 1) router.back();
  else void router.push("/");
}

function onRetry(): void {
  void refetch();
}
</script>

<template>
  <div class="flex flex-col gap-6">
    <div>
      <Button
        label="Back"
        icon="pi pi-arrow-left"
        severity="secondary"
        variant="text"
        @click="goBack"
      />
    </div>

    <div v-if="isPending && !invalidId" class="flex flex-col gap-5" aria-busy="true">
      <Skeleton class="h-9 w-2/3 rounded-lg" />
      <Skeleton class="h-80 w-full rounded-2xl" />
    </div>

    <div
      v-else-if="isError || invalidId"
      role="alert"
      class="flex flex-col items-start gap-3 rounded-xl border border-danger-100 bg-danger-50 p-5"
    >
      <p class="flex items-center gap-2 font-medium text-surface-800">
        <i class="pi pi-exclamation-triangle text-danger" aria-hidden="true" />
        {{ error?.message || "Could not load this recipe." }}
      </p>
      <!-- Retrying a gated query would refetch /api/recipes/NaN — only a real
           lookup failure can usefully be retried. -->
      <Button
        v-if="isError"
        label="Try again"
        icon="pi pi-refresh"
        severity="secondary"
        size="small"
        @click="onRetry"
      />
    </div>

    <RecipeDetail v-else-if="recipe" :recipe="recipe" :user-ingredients="ingredients" />
  </div>
</template>
