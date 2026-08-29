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
