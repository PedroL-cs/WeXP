import { apiFetch } from './apiFetch';
import type { AchievementDetails } from '../types/Achievement';

export async function getAchievement(
  achievementId: string,
  signal?: AbortSignal,
): Promise<AchievementDetails> {
  const response = await apiFetch(`/api/v1/achievements/${achievementId}`, {
    signal,
  });

  if (!response.ok) {
    throw new Error('Não foi possível carregar a conquista.');
  }

  const achievement: AchievementDetails = await response.json();
  return {
    ...achievement,
    hidden: achievement.hidden ?? achievement.isHidden,
  };
}
