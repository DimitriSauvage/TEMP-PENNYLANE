/**
 * Hand-written fetch instance used as the Orval mutator (orval 8 fetch
 * contract): takes a path-only URL, returns `{ data, status, headers }`.
 *
 * Error propagation: network failures reject (native fetch behavior); HTTP
 * error statuses are returned inside the typed result so callers discriminate
 * on `status` — the generated response types are unions of success and error
 * variants. A body that is present but not parseable JSON (e.g. an nginx
 * 502/504 HTML error page when the backend container is down) yields
 * `data: null` instead of rejecting with a raw `SyntaxError`, so the status
 * code survives; callers report it via {@link ApiError}.
 *
 * The base URL comes from public runtime config (`apiBase`, default
 * `http://localhost:8080`, overridable via `NUXT_PUBLIC_API_BASE`); it is
 * injected at app boot by `app/plugins/api.client.ts` so this module stays
 * importable outside a Nuxt context (tests, workers).
 */

/**
 * Typed error for a non-2xx API response: carries the HTTP status and either
 * the server-provided message or a fallback text when the body is unparseable
 * or carries no message.
 */
export class ApiError extends Error {
  /** HTTP status code of the failed response. */
  readonly status: number;

  /**
   * @param status - HTTP status code of the failed response.
   * @param data - Parsed response body; treated as unknown because the trust
   *   boundary can deliver `null` (unparseable body) or an empty object.
   */
  constructor(status: number, data: unknown) {
    super(readServerMessage(data) ?? `Request failed with status ${status}`);
    this.status = status;
  }
}

/** Extract a string `message` property from an unknown response body. */
function readServerMessage(data: unknown): string | undefined {
  if (typeof data === "object" && data !== null && "message" in data) {
    const { message } = data;
    if (typeof message === "string") return message;
  }
  return undefined;
}

/** Default matching `runtimeConfig.public.apiBase` in nuxt.config.ts. */
const DEFAULT_API_BASE = "http://localhost:8080";

let apiBase = DEFAULT_API_BASE;

/** Set the backend base URL; called once at app boot from the api plugin. */
export function setApiBase(base: string): void {
  apiBase = base;
}

export const customInstance = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
  const response = await fetch(`${apiBase}${url}`, options);

  const body = [204, 205, 304].includes(response.status) ? null : await response.text();
  let data: unknown = {};
  if (body) {
    try {
      data = JSON.parse(body);
    } catch {
      // Non-JSON body (e.g. an nginx 502/504 HTML error page): deliver null
      // data with the status instead of rejecting with a raw SyntaxError.
      data = null;
    }
  }
  // The `as T` cast is unavoidable here: T is the Orval-generated union of
  // success and error response shapes, which cannot be discriminated without
  // the status code the caller checks — no type guard can validate it at this
  // boundary.
  return { data, status: response.status, headers: response.headers } as T;
};
