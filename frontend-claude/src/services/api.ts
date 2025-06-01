const API_BASE = 'http://localhost:8080';

export interface ApiResponse<T = any> {
  data?: T;
  error?: string;
  status: number;
}

export const apiCall = async <T = any>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> => {
  const url = `${API_BASE}${endpoint}`;
  
  // Stringify body if it's an object and Content-Type is application/json
  const requestOptions = { ...options };
  if (requestOptions.body && 
      typeof requestOptions.body === 'object' && 
      !(requestOptions.body instanceof FormData) &&
      !(requestOptions.body instanceof URLSearchParams) &&
      !(requestOptions.body instanceof Blob) &&
      !ArrayBuffer.isView(requestOptions.body)) {
    requestOptions.body = JSON.stringify(requestOptions.body);
  }
  
  try {
    const response = await fetch(url, {
      ...requestOptions,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(requestOptions.headers || {})
      }
    });

    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`;
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
      } catch (e) {
        // If we can't parse the error as JSON, use the status text
        errorMessage = response.statusText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    // Handle empty response
    const text = await response.text();
    if (!text) {
      return null as unknown as T;
    }

    // Try to parse as JSON, but handle potential JSON parsing errors
    try {
      return JSON.parse(text) as T;
    } catch (e) {
      // If it's not JSON, return as text (for text/plain responses)
      return text as unknown as T;
    }
  } catch (error) {
    console.error('API call failed:', error);
    throw error;
  }
};

export default {
  apiCall,
};
