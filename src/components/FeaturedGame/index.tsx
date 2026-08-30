import { useEffect, useState } from 'react';
import styles from './styles.module.css';
import type { Game } from '../../types/Game';

type FeaturedGameProps = {
  games: Game[];
};

function FeaturedGame({ games }: FeaturedGameProps) {
  const [currentIndex, setCurrentIndex] = useState(0);

  const currentGame = games[currentIndex];

  const goToPrevious = () => {
    setCurrentIndex(current =>
      current === 0 ? games.length - 1 : current - 1,
    );
  };

  const goToNext = () => {
    setCurrentIndex(current => (current + 1) % games.length);
  };

  useEffect(() => {
    if (games.length <= 1) return;

    const interval = setInterval(() => {
      setCurrentIndex(current => (current + 1) % games.length);
    }, 5000);

    return () => clearInterval(interval);
  }, [games.length]);

  if (!currentGame) {
    return null;
  }

  return (
    <section className={styles.featuredGame}>
      <img
        key={currentGame.id}
        className={styles.background}
        src={currentGame.images.hero}
        alt=''
      />

      <div className={styles.overlay}>
        <h1>{currentGame.name}</h1>

        <p>{currentGame.shortDescription}</p>
      </div>

      {games.length > 1 && (
        <>
          <button
            className={`${styles.control} ${styles.previous}`}
            onClick={goToPrevious}
            aria-label='Jogo anterior'
          >
            ‹
          </button>

          <button
            className={`${styles.control} ${styles.next}`}
            onClick={goToNext}
            aria-label='Próximo jogo'
          >
            ›
          </button>

          <div className={styles.dots}>
            {games.map((game, index) => (
              <button
                key={game.id}
                className={`${styles.dot} ${
                  index === currentIndex ? styles.active : ''
                }`}
                onClick={() => setCurrentIndex(index)}
                aria-label={`Mostrar ${game.name}`}
              />
            ))}
          </div>
        </>
      )}
    </section>
  );
}

export default FeaturedGame;
