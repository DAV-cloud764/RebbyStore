import {
  apiClient,
  clearAuthToken,
  setAuthToken,
} from "./apiClient";

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
}

export async function login(
  credentials: LoginRequest,
): Promise<LoginResponse> {
  const response = await apiClient<LoginResponse>("/api/auth/login", {
    method: "POST",
    authenticated: false,
    body: JSON.stringify(credentials),
  });

  setAuthToken(response.accessToken);

  return response;
}

export function logout(): void {
  clearAuthToken();
}