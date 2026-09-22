const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(
    status: number,
    message: string,
    fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

function getAuthToken(): string | null {
  return sessionStorage.getItem("rebbystore_token");
}

export function setAuthToken(token: string): void {
  sessionStorage.setItem("rebbystore_token", token);
}

export function clearAuthToken(): void {
  sessionStorage.removeItem("rebbystore_token");
}

export function hasAuthToken(): boolean {
  return Boolean(getAuthToken());
}

type RequestOptions = RequestInit & {
  authenticated?: boolean;
};

export async function apiClient<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { authenticated = true, ...fetchOptions } = options;

  const headers = new Headers(fetchOptions.headers);

  headers.set("Accept", "application/json");

  if (fetchOptions.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (authenticated) {
    const token = getAuthToken();

    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...fetchOptions,
    headers,
  });

  const contentType = response.headers.get("content-type") ?? "";

  let responseBody: unknown = null;

  if (response.status !== 204) {
    if (contentType.includes("application/json")) {
      responseBody = await response.json();
    } else {
      responseBody = await response.text();
    }
  }

  if (!response.ok) {
    const errorBody =
      typeof responseBody === "object" && responseBody !== null
        ? (responseBody as {
            message?: string;
            fieldErrors?: Record<string, string>;
          })
        : {};

    throw new ApiError(
      response.status,
      errorBody.message || `Request failed with status ${response.status}`,
      errorBody.fieldErrors || {},
    );
  }

  return responseBody as T;
}