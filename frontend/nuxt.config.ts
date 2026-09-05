import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  ssr: false,
  compatibilityDate: "2026-09-04",
  modules: ["@nuxt/eslint", "@primevue/nuxt-module"],
  app: {
    head: {
      htmlAttrs: { lang: "en" },
    },
  },
  // Route-scoped components colocated under pages/*/components are explicit
  // imports, not routes: exclude them from the pages scan.
  pages: {
    pattern: ["**/*.vue", "!**/components/**"],
  },
  css: ["primeicons/primeicons.css", "~/assets/css/main.css"],
  vue: {
    compilerOptions: {
      isCustomElement: (tag) => tag === "search",
    },
  },
  // pathPrefix: false keeps folder-per-component names clean
  // (components/recipe-card/recipe-card.vue -> <RecipeCard>).
  components: [{ path: "~/components", pathPrefix: false }],
  vite: {
    plugins: [tailwindcss()],
  },
  primevue: {
    importTheme: { from: "@primeuix/themes/aura" },
    // options.theme (the shape PrimeVue wants) is mangled by runtimeConfig
    // serialization; theme fixed at runtime in app/plugins/primevue-theme.client.ts.
  },
  runtimeConfig: {
    public: {
      /** Backend base URL; override with NUXT_PUBLIC_API_BASE. */
      apiBase: "http://localhost:8080",
    },
  },
  typescript: {
    strict: true,
  },
});
