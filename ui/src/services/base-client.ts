/**
 * Base service client for making requests to backend services.
 * All service clients extend this class to share common functionality.
 * 
 * Note: Currently configured without authentication for demo mode.
 */

export interface ServiceError {
  status: number;
  error: string;
  message: string;
}

export class ServiceClientError extends Error {
  constructor(
    public status: number,
    public error: string,
    message: string
  ) {
    super(message);
    this.name = "ServiceClientError";
  }
}

export abstract class BaseServiceClient {
  protected readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  /**
   * Make a GET request.
   */
  protected async get<T>(path: string): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "GET",
      headers: this.createHeaders(),
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Make a POST request.
   */
  protected async post<T, B = unknown>(path: string, body?: B): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "POST",
      headers: this.createHeaders(),
      body: body ? JSON.stringify(body) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Make a PUT request.
   */
  protected async put<T, B = unknown>(path: string, body?: B): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "PUT",
      headers: this.createHeaders(),
      body: body ? JSON.stringify(body) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Make a DELETE request.
   */
  protected async delete<T>(path: string): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "DELETE",
      headers: this.createHeaders(),
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Create headers for the request.
   * No authentication in demo mode.
   */
  private createHeaders(): HeadersInit {
    return {
      "Content-Type": "application/json",
    };
  }

  /**
   * Handle the response from the backend service.
   */
  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let errorData: ServiceError;
      try {
        errorData = await response.json();
      } catch {
        errorData = {
          status: response.status,
          error: response.statusText,
          message: "An unexpected error occurred",
        };
      }

      throw new ServiceClientError(
        response.status,
        errorData.error,
        errorData.message
      );
    }

    // Handle empty responses (204 No Content or empty body)
    if (response.status === 204) {
      return undefined as T;
    }

    // Check if response has content before parsing JSON
    const contentLength = response.headers.get("content-length");
    const contentType = response.headers.get("content-type");
    
    // If no content-length or it's 0, or no JSON content-type, return undefined
    if (contentLength === "0" || (!contentType?.includes("application/json"))) {
      return undefined as T;
    }

    // Try to parse JSON, but handle empty bodies gracefully
    const text = await response.text();
    if (!text || text.trim() === "") {
      return undefined as T;
    }

    return JSON.parse(text) as T;
  }
}
