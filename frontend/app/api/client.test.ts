import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, customInstance, setApiBase } from "./client";

/** Minimal response envelope matching the Orval mutator contract. */
interface TestResponse {
  data: unknown;
  status: number;
  headers: Headers;
}

describe("customInstance", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("parses a JSON body into { data, status, headers }", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ names: ["egg"] }), { status: 200 }),
    );
    const result = await customInstance<TestResponse>("/api/ingredients/suggest?q=e");
    expect(result.data).toEqual({ names: ["egg"] });
    expect(result.status).toBe(200);
    expect(result.headers).toBeInstanceOf(Headers);
  });

  it("returns data null when the body is not JSON (nginx 502/504 HTML page)", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response("<html><body>502 Bad Gateway</body></html>", { status: 502 }),
    );
    const result = await customInstance<TestResponse>("/api/recipes/search");
    expect(result.data).toBeNull();
    expect(result.status).toBe(502);
  });

  it("returns an empty-object data for an empty body", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response("", { status: 200 }));
    const result = await customInstance<TestResponse>("/api/recipes/search");
    expect(result.data).toEqual({});
    expect(result.status).toBe(200);
  });

  it("returns an empty-object data for a 204 without reading the body", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 204 }));
    const result = await customInstance<TestResponse>("/api/recipes/search");
    expect(result.data).toEqual({});
    expect(result.status).toBe(204);
  });

  it("keeps the parsed error payload and status on a non-2xx JSON error body", async () => {
    const errorBody = {
      code: "NO_USABLE_INGREDIENTS",
      message: "Provide at least one usable ingredient.",
    };
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(errorBody), { status: 422 }));
    const result = await customInstance<TestResponse>("/api/recipes/search");
    expect(result.data).toEqual(errorBody);
    expect(result.status).toBe(422);
  });

  describe("base URL joining", () => {
    it("joins the configured base with the path-only URL", async () => {
      setApiBase("http://backend.example");
      vi.mocked(fetch).mockResolvedValue(new Response("{}", { status: 200 }));
      await customInstance<TestResponse>("/api/recipes/search");
      expect(fetch).toHaveBeenCalledWith("http://backend.example/api/recipes/search", {});
    });

    it("keeps the URL relative (same-origin) when the base is empty", async () => {
      setApiBase("");
      vi.mocked(fetch).mockResolvedValue(new Response("{}", { status: 200 }));
      await customInstance<TestResponse>("/api/recipes/search");
      expect(fetch).toHaveBeenCalledWith("/api/recipes/search", {});
    });

    it("forwards the request options to fetch", async () => {
      setApiBase("");
      vi.mocked(fetch).mockResolvedValue(new Response("{}", { status: 200 }));
      await customInstance<TestResponse>("/api/recipes/search", { method: "GET" });
      expect(fetch).toHaveBeenCalledWith("/api/recipes/search", { method: "GET" });
    });
  });
});

describe("ApiError", () => {
  it("carries the HTTP status", () => {
    expect(new ApiError(404, null)).toHaveProperty("status", 404);
  });

  it("uses the server message when the body carries one", () => {
    const error = new ApiError(422, {
      code: "NO_USABLE_INGREDIENTS",
      message: "Provide at least one usable ingredient.",
    });
    expect(error.message).toBe("Provide at least one usable ingredient.");
  });

  it("falls back to a status text when data is null (unparseable body)", () => {
    expect(new ApiError(502, null).message).toBe("Request failed with status 502");
  });

  it("falls back to a status text when the body has no message", () => {
    expect(new ApiError(502, {}).message).toBe("Request failed with status 502");
  });

  it("ignores a non-string message", () => {
    expect(new ApiError(500, { message: 42 }).message).toBe("Request failed with status 500");
  });
});
