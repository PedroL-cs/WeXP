import { useEffect, useState } from 'react';

import { searchGames } from '../../api/games';
import type { Game } from '../../types/Game';

import { useSearchParams } from 'react-router';
import { MagnifyingGlassIcon } from '@phosphor-icons/react';

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
          <p className={styles.eyebrow}>Resultados da busca</p>
          <h2>
            <span>“</span>
            {query}
            <span>”</span>
          </h2>

          {!isLoading && totalElements > 0 && (
            <span className={styles.count}>
              {totalElements}{' '}
              {totalElements === 1 ? 'jogo encontrado' : 'jogos encontrados'}
            </span>
          )}
        </div>

        {isLoading ? (
          <div className={styles.results} aria-busy='true'>
            {Array.from({ length: 4 }, (_, index) => (
              <div key={index} className={styles.skeleton} />
            ))}
          </div>
        ) : games.length === 0 ? (
          <div className={styles.status}>
            <MagnifyingGlassIcon size={36} weight='duotone' />
            <h3>Nenhum jogo encontrado</h3>
            <p>
              Não encontramos jogos para "{query}". Confira a ortografia ou
              tente um termo mais curto.
            </p>
          </div>
        ) : (
          <div className={styles.results}>
            {games.map((game, index) => (
              <SearchGameCard
                key={game.id}
                game={game}
                style={{ animationDelay: `${Math.min(index, 10) * 40}ms` }}
              />
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

export default SearchPage;
