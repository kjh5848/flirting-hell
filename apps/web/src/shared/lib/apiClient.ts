import type { ApiResponse } from "@flirting-hell/shared";
import { getAnonymousUserId } from "./storage";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "";

export class ApiClientError extends Error {
  constructor(public readonly code: string, message: string) {
    super(message);
    this.name = "ApiClientError";
  }
}

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
};

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers: {
      "content-type": "application/json",
      "x-anonymous-user-id": getAnonymousUserId(),
      ...options.headers
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  });

  const payload = (await response.json()) as ApiResponse<T>;

  if (!payload.ok) {
    throw new ApiClientError(payload.error.code, payload.error.message);
  }

  return payload.data;
}
