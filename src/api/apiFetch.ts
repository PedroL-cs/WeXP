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

  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  return fetch(input, {
    ...init,
    headers,
  });
}
