import { useEffect, useState } from 'react';

import FeaturedGame from '../../components/FeaturedGame';
import GameCarousel from '../../components/GameCarousel';

import { getFeaturedGames, getReleasedGames } from '../../api/games';

import type { Game } from '../../types/Game';

import styles from './styles.module.css';

export function HomePage() {
  const [featuredGames, setFeaturedGames] = useState<Game[]>([]);
  const [releasedGames, setReleasedGames] = useState<Game[]>([]);

  useEffect(() => {
    async function loadHome() {
      try {
        const [featured, released] = await Promise.all([
          getFeaturedGames(),
          getReleasedGames(),
        ]);

        setFeaturedGames(featured);
        setReleasedGames(released);
      } catch (error) {
        console.error('Erro ao carregar Home:', error);
      }
    }

    loadHome();
  }, []);

  const sliderGames = featuredGames.slice(0, 5);
  const remainingFeaturedGames = featuredGames.slice(5);

  return (
    <section className={styles.homePage}>
      {sliderGames.length > 0 && (
        <>
          <h2>Destaques</h2>
          <FeaturedGame games={sliderGames} />
        </>
      )}

      {remainingFeaturedGames.length > 0 && (
        <>
          <h2 className={styles.section}>Mais destaques</h2>
          <GameCarousel games={remainingFeaturedGames} />
        </>
      )}

      {releasedGames.length > 0 && (
        <>
          <h2 className={styles.section}>Novos lançamentos</h2>
          <GameCarousel games={releasedGames} />
        </>
      )}
    </section>
  );
}
