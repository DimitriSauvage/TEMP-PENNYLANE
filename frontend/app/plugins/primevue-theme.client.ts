import { Theme } from "@primeuix/styled";
import Aura from "@primeuix/themes/aura";
import { definePreset } from "@primeuix/themes";

/**
 * Warm "kitchen" variant of Aura, derived purely from preset primitives:
 * a burnt-orange primary (amber/orange palettes) and stone-tinted surfaces,
 * so the app reads warm and food-friendly instead of cool indigo/slate.
 * The 500/600/700 shades sit a step deeper than the palette default so
 * {primary.color} keeps white labels at ≥ 4.5:1 contrast.
 */
const WarmAura = definePreset(Aura, {
  semantic: {
    primary: {
      50: "{amber.50}",
      100: "{amber.100}",
      200: "{amber.200}",
      300: "{amber.300}",
      400: "{amber.400}",
      500: "{orange.700}",
      600: "{orange.800}",
      700: "{orange.900}",
      800: "{orange.900}",
      900: "{orange.950}",
      950: "{orange.950}",
    },
    colorScheme: {
      light: {
        surface: {
          0: "#ffffff",
          50: "{stone.50}",
          100: "{stone.100}",
          200: "{stone.200}",
          300: "{stone.300}",
          400: "{stone.400}",
          500: "{stone.500}",
          600: "{stone.600}",
          700: "{stone.700}",
          800: "{stone.800}",
          900: "{stone.900}",
          950: "{stone.950}",
        },
      },
    },
  },
});

/**
 * SPA mode: @primevue/nuxt-module ships the PrimeVue design tokens
 * (`:root { --p-* }` CSS custom properties) only through a Nitro
 * `render:html` hook, i.e. during SSR. With `ssr: false` nothing ever
 * defines them, so every themed color resolves to nothing and the whole
 * app renders unstyled.
 *
 * The module also feeds the bare Aura preset into `Theme.setTheme`, which
 * expects `{ preset, options }` — with the bare shape `theme.preset` stays
 * empty and the token sheets come out as "undefined". Re-setting the theme
 * in the correct shape before the first component mount makes both the
 * common variables and the component-level tokens resolve properly.
 */
export default defineNuxtPlugin(() => {
  // Light-only (darkModeSelector none) matches main.css: no dark overrides.
  Theme.setTheme({ preset: WarmAura, options: { darkModeSelector: "none" } });
  document.head.insertAdjacentHTML("beforeend", Theme.getCommonStyleSheet("", undefined, {}));
});
