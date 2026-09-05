import { setApiBase } from "~/api/client";

export default defineNuxtPlugin(() => {
  setApiBase(useRuntimeConfig().public.apiBase);
});
