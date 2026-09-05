import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { ApiError } from "~/api/client";

export default defineNuxtPlugin((nuxtApp) => {
  // 4xx responses (404 stale link, 422 unusable filters) are deterministic, so
  // retrying them with backoff only delays the error state without any chance
  // of success. 5xx responses and network failures (fetch rejects with a
  // TypeError) are transient, so they are retried up to twice before the
  // error state is surfaced.
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: (failureCount, error) =>
          !(error instanceof ApiError && error.status < 500) && failureCount < 2,
      },
    },
  });
  nuxtApp.vueApp.use(VueQueryPlugin, { queryClient });
});
