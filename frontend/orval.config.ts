import { defineConfig } from "orval";

export default defineConfig({
  recipeFinder: {
    // Pinned spec artifact dumped from the backend — never generate against a live server.
    input: "./openapi/api.json",
    output: {
      target: "./app/api/generated/client.ts",
      client: "fetch",
      mode: "tags-split",
      // Path-only URLs; the base URL is injected at runtime by the mutator.
      baseUrl: "",
      override: {
        // orval 8 requires operationName as a function, not a string.
        operations: {
          get: { operationName: () => "getRecipe" },
          search: { operationName: () => "searchRecipes" },
          suggest: { operationName: () => "suggestIngredients" },
        },
        mutator: {
          path: "./app/api/client.ts",
          name: "customInstance",
        },
      },
    },
  },
});
