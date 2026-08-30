import { useEffect, useState } from 'react';

import { searchGames } from '../../api/games';
import type { Game } from '../../types/Game';

import { useSearchParams } from 'react-router';

import styles from './styles.module.css';
import SearchGameCard from '../../components/SearchGameCard';

function SearchPage() {
  const [searchParams] = useSearchParams();

  const query = searchParams.get('q')?.trim() ?? '';

  const [games, setGames] = useState<Game[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    async function loadGames() {
      if (!query) {
        setGames([]);
        setTotalElements(0);
        return;
      }

      try {
        setIsLoading(true);

        const data = await searchGames(query);

        setGames(data.content);
        setTotalElements(data.totalElements);
      } catch (error) {
        console.error('Erro ao buscar jogos:', error);
        setGames([]);
        setTotalElements(0);
      } finally {
        setIsLoading(false);
      }
    }

    loadGames();
  }, [query]);

  return (
    <main className={styles.searchPage}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h2>Resultados para "{query}"</h2>

          {!isLoading && totalElements > 0 && (
            <span>
              {totalElements}{' '}
              {totalElements === 1 ? 'jogo encontrado' : 'jogos encontrados'}
            </span>
          )}
        </div>

        {isLoading ? (
          <div className={styles.status}>Carregando...</div>
        ) : games.length === 0 ? (
          <div className={styles.status}>
            <h3>Nenhum jogo encontrado</h3>
            <p>Não encontramos jogos para "{query}".</p>
          </div>
        ) : (
          <div className={styles.results}>
            {games.map(game => (
              <SearchGameCard key={game.id} game={game} />
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

export default SearchPage;
