import type { Game } from '../types/Game';
import type { SteamGame } from '../types/SteamGame';
import { apiFetch } from './apiFetch';
import type { Guide, GuideRevision } from '../types/Guide';

export async function instantSearchGames(query: string): Promise<SteamGame[]> {
  const params = new URLSearchParams({
    q: query,
    page: '0',
  });

  const response = await fetch(
    `/api/v1/games/steam-games?${params.toString()}`,
  );

  if (!response.ok) {
    throw new Error('Erro ao buscar jogos');
  }

  const data = await response.json();

  return data.content;
}

export async function getGameIdBySteamAppId(steamAppId: number) {
  const response = await fetch(`/api/v1/games/steam/${steamAppId}/id`);

  if (!response.ok) {
    throw new Error('Erro ao buscar ID do jogo');
  }

  return response.json();
}

export async function searchGames(query: string, page = 0) {
  const params = new URLSearchParams({
    q: query,
    page: page.toString(),
  });

  const response = await fetch(`/api/v1/games?${params.toString()}`);

  if (!response.ok) {
    throw new Error('Erro ao buscar jogos');
  }

  return response.json();
}

export async function getGame(id: string): Promise<Game> {
  const response = await fetch(`/api/v1/games/${id}`);

  if (!response.ok) {
    throw new Error('Não foi possível carregar o jogo.');
  }

  return response.json();
}

export async function getFeaturedGames(): Promise<Game[]> {
  const response = await fetch('/api/v1/games/featured');

  if (!response.ok) {
    throw new Error('Erro ao buscar jogos em destaque');
  }

  const data = await response.json();

  return data.content;
}

export async function getReleasedGames(): Promise<Game[]> {
  const response = await fetch('/api/v1/games/released');

  if (!response.ok) {
    throw new Error('Erro ao buscar jogos mais recentes');
  }

  const data = await response.json();

  return data.content;
}

export async function createAchievementGuide(
  achievementId: string,
  markdown: string,
) {
  const response = await apiFetch(
    `/api/v1/achievements/${achievementId}/guide`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        content: markdown,
      }),
    },
  );

  if (!response.ok) {
    throw new Error('Não foi possível criar o guia.');
  }

  return response.json();
}

export async function getAchievementGuide(achievementId: string): Promise<Guide> {
  const response = await apiFetch(
    `/api/v1/achievements/${achievementId}/guide`,
  );

  if (!response.ok) {
    throw new Error('Não foi possível carregar o guia.');
  }

  const data = await response.json();

  if (typeof data === 'string') return { id: '', content: data };

  return {
    id: data.id ?? data.publicId ?? '',
    content: data.content ?? data.markdown ?? '',
    version: data.version,
    achievementId: data.achievementId ?? data.achievementPublicId,
    authorUsername: data.authorUsername,
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  };
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

export async function getGuideRevisions(guideId: string): Promise<GuideRevision[]> {
  const response = await apiFetch(`/api/v1/guides/${guideId}/revisions`);

  if (!response.ok) throw new Error('Não foi possível carregar as revisões.');

  const data = await response.json();
  return Array.isArray(data) ? data : data.content ?? [];
}
