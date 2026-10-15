const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? '';

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

  return fetch(requestInput, {
    ...init,
    headers,
  });
}
