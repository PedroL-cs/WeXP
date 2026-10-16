const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? '';
export const AUTH_EXPIRED_EVENT = 'auth-expired';

export function apiUrl(path: string) {
  return `${apiBaseUrl}${path}`;
}

export async function apiFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
) {
  const token =
    localStorage.getItem('token') ?? sessionStorage.getItem('token');

  const headers = new Headers(init.headers);

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  if (
    init.body &&
    !(init.body instanceof FormData) &&
    !headers.has('Content-Type')
  ) {
    headers.set('Content-Type', 'application/json');
  }

  const requestInput = typeof input === 'string' ? apiUrl(input) : input;

  const response = await fetch(requestInput, {
    ...init,
    headers,
  });

  if (response.status === 401 && token) {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
  }

  return response;
}
