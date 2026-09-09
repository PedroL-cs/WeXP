import { apiFetch } from './apiFetch';

export async function getAchievement(achievementId: string) {
  const response = await apiFetch(`/api/v1/achievements/${achievementId}`);

  if (!response.ok) {
    throw new Error('Não foi possível carregar a conquista.');
  }

  return response.json();
}
