import { apiUrl } from './apiFetch';
import { ApiError, throwApiError } from './apiError';

async function authRequest(path: string, body: object, fallback: string) {
  let response: Response;
  try {
    // Public auth requests must not attach a stale session token.
    response = await fetch(apiUrl(path), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.', 0);
  }
  if (response.status >= 500) {
    throw new ApiError('O serviço está temporariamente indisponível. Tente novamente em instantes.', response.status);
  }
  if (!response.ok) {
    return throwApiError(response, response.status === 401 ? 'Usuário ou senha inválidos.' : fallback);
  }

  const data = await response.json().catch(() => null);
  if (typeof data?.token !== 'string' || !data.token.trim()) {
    throw new ApiError('Não foi possível confirmar sua sessão. Tente novamente.', response.status);
  }
  return data;
}

export async function login(login: string, password: string) {
  return authRequest('/api/v1/auth/login', { login, password }, 'Não foi possível entrar. Confira seus dados e tente novamente.');
}

export async function register(
  username: string,
  email: string,
  login: string,
  password: string,
  birthDate: string,
) {
  return authRequest('/api/v1/auth/register', {
    username,
    email,
    login,
    password,
    birthDate,
  }, 'Não foi possível criar sua conta. Confira os dados e tente novamente.');
}
