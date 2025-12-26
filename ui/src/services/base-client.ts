/**
 * Base service client for making authenticated requests to backend services.
 * All service clients extend this class to share common functionality.
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
   * Make an authenticated GET request.
   */
  protected async get<T>(
    path: string,
    accessToken: string
  ): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "GET",
      headers: this.createHeaders(accessToken),
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Make an authenticated POST request.
   */
  protected async post<T, B = unknown>(
    path: string,
    accessToken: string,
    body?: B
  ): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "POST",
      headers: this.createHeaders(accessToken),
      body: body ? JSON.stringify(body) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Make an authenticated PUT request.
   */
  protected async put<T, B = unknown>(
    path: string,
    accessToken: string,
    body?: B
  ): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "PUT",
      headers: this.createHeaders(accessToken),
      body: body ? JSON.stringify(body) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Make an authenticated DELETE request.
   */
  protected async delete<T>(
    path: string,
    accessToken: string
  ): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: "DELETE",
      headers: this.createHeaders(accessToken),
    });

    return this.handleResponse<T>(response);
  }

  /**
   * Create authorization headers for the request.
   */
  private createHeaders(accessToken: string): HeadersInit {
    return {
      Authorization: `Bearer ${accessToken}`,
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

    // Handle empty responses (204 No Content)
    if (response.status === 204) {
      return undefined as T;
    }

    return response.json();
  }
}

