import type { Game } from '../types/Game';
import type { SteamGame } from '../types/SteamGame';

export async function instantSearchGames(query: string): Promise<SteamGame[]> {
  const params = new URLSearchParams({
    q: query,
    page: '0',
    size: '5',
  });

  const response = await fetch(
    `/api/v1/games/search/instant?${params.toString()}`,
  );

  if (!response.ok) {
    throw new Error('Erro ao buscar jogos');
  }

  const data = await response.json();

  return data.content;
}

export async function searchGames(query: string, page = 0, size = 20) {
  const params = new URLSearchParams({
    q: query,
    page: page.toString(),
    size: size.toString(),
  });

  const response = await fetch(`/api/v1/games/search?${params.toString()}`);

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
