import { describe, expect, it, vi } from "vitest";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { ApiError } from "~/api/client";
import plugin from "./vue-query.client";

describe("vue-query plugin", () => {
  it("installs VueQueryPlugin with a QueryClient", () => {
    const use = vi.fn<(installed: unknown, options?: unknown) => void>();
    // Minimal NuxtApp stub: the plugin only touches `vueApp.use`, so a full
    // Nuxt app context is not needed here (no type guard can provide one).
    plugin({ vueApp: { use } } as never);

    expect(use).toHaveBeenCalledTimes(1);
    const [installed, options] = use.mock.calls[0]!;
    expect(installed).toBe(VueQueryPlugin);
    const { queryClient } = options as { queryClient: QueryClient };
    expect(queryClient).toBeInstanceOf(QueryClient);
  });

  it("retries 5xx and network errors, never 4xx errors", () => {
    const use = vi.fn<(installed: unknown, options?: unknown) => void>();
    plugin({ vueApp: { use } } as never);
    const [, options] = use.mock.calls[0]!;
    const { queryClient } = options as { queryClient: QueryClient };
    const retry = queryClient.getDefaultOptions().queries?.retry as (
      failureCount: number,
      error: Error,
    ) => boolean;

    // 4xx are deterministic: never retried.
    expect(retry(0, new ApiError(404, null))).toBe(false);
    expect(retry(0, new ApiError(422, { message: "x" }))).toBe(false);
    // 5xx are transient: retried up to twice.
    expect(retry(0, new ApiError(503, null))).toBe(true);
    expect(retry(1, new ApiError(503, null))).toBe(true);
    expect(retry(2, new ApiError(503, null))).toBe(false);
    // Network failures (fetch rejects with TypeError) are transient too.
    expect(retry(0, new TypeError("Failed to fetch"))).toBe(true);
  });
});
