import type { CurrentUser, PublicUser, UpdateUserData } from '../types/User';
import { apiFetch } from './apiFetch';
import { throwApiError } from './apiError';

export async function getCurrentUser(
  signal?: AbortSignal,
): Promise<CurrentUser> {
  const response = await apiFetch('/api/v1/users/me', { signal });

  if (!response.ok) {
    return throwApiError(response, 'Não foi possível carregar seu perfil.');
  }

  return response.json();
}

export async function getPublicUser(
  userId: string,
  signal?: AbortSignal,
): Promise<PublicUser> {
  const response = await apiFetch(`/api/v1/users/${userId}`, { signal });

  if (!response.ok) {
    throw new Error('Não foi possível carregar este perfil.');
  }

  return response.json();
}

export const findUserById = getPublicUser;

export async function updateCurrentUser(
  data: UpdateUserData,
  avatar?: File,
): Promise<CurrentUser> {
  const body = new FormData();
  body.append(
    'data',
    new Blob([JSON.stringify(data)], { type: 'application/json' }),
  );

  if (avatar) {
    body.append('avatar', avatar);
  }

  const response = await apiFetch('/api/v1/users/me', {
    method: 'PATCH',
    body,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message ?? 'Não foi possível salvar o perfil.');
  }

  return response.json();
}
