import type { PageResponse } from '../types/PageResponse';
import type {
  SavedItem,
  SavedResourceType,
  SavedStatus,
} from '../types/SavedItem';
import { apiFetch } from './apiFetch';

const paths: Record<SavedResourceType, string> = {
  GAME: 'games',
  ACHIEVEMENT: 'achievements',
  GUIDE: 'guides',
};

function itemPath(type: SavedResourceType, resourceId: string) {
  return `/api/v1/users/me/saved-items/${paths[type]}/${encodeURIComponent(resourceId)}`;
}

export async function getSavedItems(
  type?: SavedResourceType,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<PageResponse<SavedItem>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (type) params.set('type', type);

  const response = await apiFetch(
    `/api/v1/users/me/saved-items?${params.toString()}`,
    { signal },
  );
  if (!response.ok) throw new Error('Não foi possível carregar seus itens salvos.');
  return response.json();
}

export async function getSavedStatus(
  type: SavedResourceType,
  resourceId: string,
  signal?: AbortSignal,
): Promise<SavedStatus> {
  const response = await apiFetch(itemPath(type, resourceId), { signal });
  if (!response.ok) throw new Error('Não foi possível verificar este item.');
  return response.json();
}

export async function saveItem(
  type: SavedResourceType,
  resourceId: string,
): Promise<SavedStatus> {
  const response = await apiFetch(itemPath(type, resourceId), { method: 'PUT' });
  if (!response.ok) throw new Error('Não foi possível salvar este item.');
  return response.json();
}

export async function removeSavedItem(
  type: SavedResourceType,
  resourceId: string,
): Promise<void> {
  const response = await apiFetch(itemPath(type, resourceId), {
    method: 'DELETE',
  });
  if (!response.ok) throw new Error('Não foi possível remover este item.');
}
