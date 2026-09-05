<script setup lang="ts">
useHead({
  title: "Recipe Finder",
});
</script>

<template>
  <div class="flex min-h-dvh flex-col bg-surface-100">
    <a
      href="#main-content"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-lg focus:bg-primary focus:px-4 focus:py-2 focus:text-primary-contrast"
    >
      Skip to main content
    </a>
    <header
      class="sticky top-0 z-40 border-b border-surface-200 bg-surface-0/90 backdrop-blur-sm"
    >
      <div class="mx-auto w-full max-w-5xl px-4 py-3">
        <NuxtLink
          to="/"
          class="inline-flex rounded-xl focus-visible:outline-2 focus-visible:outline-primary"
        >
          <AppTitle />
        </NuxtLink>
      </div>
    </header>
    <main id="main-content" class="mx-auto w-full max-w-5xl flex-1 px-4 py-8 pb-16">
      <NuxtPage />
    </main>
    <footer class="border-t border-surface-200 bg-surface-0">
      <p class="mx-auto w-full max-w-5xl px-4 py-4 text-center text-sm text-muted">
        Happy cooking — bon appétit!
      </p>
    </footer>
  </div>
</template>
