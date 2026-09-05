import eslint from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import skipFormatting from "eslint-config-prettier";
import sonarjs from "eslint-plugin-sonarjs";
import tseslint from "typescript-eslint";
import withNuxt from "./.nuxt/eslint.config.mjs";

export default withNuxt(
  {
    ignores: [
      "app/api/generated/**",
      ".nuxt/**",
      ".output/**",
      "dist/**",
      "coverage/**",
      "node_modules/**",
    ],
  },
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs["flat/recommended"],
  {
    files: ["**/*.vue"],
    languageOptions: {
      parserOptions: { parser: tseslint.parser },
    },
  },
  {
    rules: {
      "@typescript-eslint/ban-ts-comment": [
        "error",
        {
          "ts-expect-error": { descriptionFormat: "^: .{10,}$" },
          "ts-ignore": true,
          "ts-nocheck": true,
          minimumDescriptionLength: 10,
        },
      ],
    },
  },
  {
    // Page files are route entries (index.vue, [id].vue), not reusable components.
    // error.vue is a Nuxt app entry file, not a reusable component either.
    files: ["app/pages/**/*.vue", "app/error.vue"],
    rules: {
      "vue/multi-word-component-names": "off",
    },
  },
  {
    files: ["app/**/*.{js,ts,vue}"],
    plugins: { sonarjs },
    rules: {
      "sonarjs/cognitive-complexity": ["error", 9],
    },
  },
  skipFormatting,
);
