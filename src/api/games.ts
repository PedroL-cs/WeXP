import type { Game } from '../types/Game';
import type { SteamGame } from '../types/SteamGame';

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
