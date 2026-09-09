import type { Guide, GuideRevision } from '../types/Guide';
import { apiFetch } from './apiFetch';

export async function createAchievementGuide(
  achievementId: string,
  content: string,
) {
  const response = await apiFetch(
    `/api/v1/achievements/${achievementId}/guide`,
    { method: 'POST', body: JSON.stringify({ content }) },
  );
  if (!response.ok) throw new Error('Não foi possível criar o guia.');
  return response.json();
}

export async function getAchievementGuide(
  achievementId: string,
): Promise<Guide | null> {
  const response = await apiFetch(
    `/api/v1/achievements/${achievementId}/guide`,
  );

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    throw new Error('Não foi possível carregar o guia.');
  }

  return response.json();
}

export async function createGuideRevision(
  guideId: string,
  content: string,
  changeSummary: string,
): Promise<GuideRevision> {
  const response = await apiFetch(`/api/v1/guides/${guideId}/revisions`, {
    method: 'POST',
    body: JSON.stringify({ content, changeSummary }),
  });
  if (!response.ok) throw new Error('Não foi possível enviar a revisão.');
  return response.json();
}

export async function getGuideRevisions(
  guideId: string,
): Promise<GuideRevision[]> {
  const response = await apiFetch(`/api/v1/guides/${guideId}/revisions`);
  if (!response.ok) throw new Error('Não foi possível carregar as revisões.');
  const data = await response.json();
  return Array.isArray(data) ? data : (data.content ?? []);
}
