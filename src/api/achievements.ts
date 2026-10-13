import { apiFetch } from './apiFetch';

export async function getAchievement(
  achievementId: string,
  signal?: AbortSignal,
) {
  const response = await apiFetch(`/api/v1/achievements/${achievementId}`, {
    signal,
  });

  if (!response.ok) {
    throw new Error('Não foi possível carregar a conquista.');
  }

  return response.json();
}
