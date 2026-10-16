import { useEffect, useState } from 'react';
import { RocketLaunchIcon, SparkleIcon } from '@phosphor-icons/react';

import FeaturedGame from '../../components/FeaturedGame';
import GameCarousel from '../../components/GameCarousel';

import { getFeaturedGames, getReleasedGames } from '../../api/games';

import type { Game } from '../../types/Game';

import styles from './styles.module.css';

export function HomePage() {
  const [featuredGames, setFeaturedGames] = useState<Game[]>([]);
  const [releasedGames, setReleasedGames] = useState<Game[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    async function loadHome() {
      setIsLoading(true);
      setError(null);

      try {
        const [featured, released] = await Promise.all([
          getFeaturedGames(),
          getReleasedGames(),
        ]);

        setFeaturedGames(featured);
        setReleasedGames(released);
      } catch (error) {
        console.error('Erro ao carregar Home:', error);
        setError('Não foi possível carregar os jogos. Tente novamente.');
      } finally {
        setIsLoading(false);
      }
    }

    loadHome();
  }, [retry]);

  const sliderGames = featuredGames.slice(0, 5);
  const remainingFeaturedGames = featuredGames.slice(5);

  if (isLoading) {
    return (
      <section className={styles.homePage} aria-busy='true'>
        <div className={`${styles.skeleton} ${styles.skeletonHero}`} />
        <div className={styles.skeletonRow}>
          {Array.from({ length: 4 }, (_, index) => (
            <div key={index} className={styles.skeleton} />
          ))}
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className={styles.homePage}>
        <div className={styles.errorState} role='alert'>
          <p>{error}</p>
          <button type='button' onClick={() => setRetry(value => value + 1)}>
            Tentar novamente
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className={styles.homePage}>
      {sliderGames.length > 0 && <FeaturedGame games={sliderGames} />}

      {remainingFeaturedGames.length > 0 && (
        <div className={styles.section}>
          <header className={styles.sectionHeader}>
            <SparkleIcon size={20} weight='fill' />
            <h2>Mais destaques</h2>
          </header>
          <GameCarousel games={remainingFeaturedGames} />
        </div>
      )}

      {releasedGames.length > 0 && (
        <div className={styles.section}>
          <header className={styles.sectionHeader}>
            <RocketLaunchIcon size={20} weight='fill' />
            <h2>Novos lançamentos</h2>
          </header>
          <GameCarousel games={releasedGames} />
        </div>
      )}
    </section>
  );
}
