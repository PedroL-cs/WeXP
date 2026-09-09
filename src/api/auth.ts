import { apiFetch } from './apiFetch';

export async function login(login: string, password: string) {
  const response = await apiFetch('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      login,
      password,
    }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message ?? 'Usuário ou senha inválidos.');
  }

  return data;
}

export async function register(
  username: string,
  email: string,
  login: string,
  password: string,
  birthDate: string,
) {
  const response = await apiFetch('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify({
      username,
      email,
      login,
      password,
      birthDate,
    }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message ?? 'Erro ao criar usuário.');
  }

  return data;
}
