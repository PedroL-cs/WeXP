import { useState } from 'react';
import GameCard from '../GameCard';
import type { Game } from '../../types/Game';
import styles from './styles.module.css';

type GameCarouselProps = {
  games: Game[];
};

const VISIBLE_GAMES = 4;
const STEP = 2;

function GameCarousel({ games }: GameCarouselProps) {
  const [startIndex, setStartIndex] = useState(0);

  const maxIndex = Math.max(0, games.length - VISIBLE_GAMES);

  const canGoPrevious = startIndex > 0;
  const canGoNext = startIndex < maxIndex;

  const goToPrevious = () => {
    setStartIndex(current => Math.max(0, current - STEP));
  };

  const goToNext = () => {
    setStartIndex(current => Math.min(maxIndex, current + STEP));
  };

  if (games.length === 0) {
    return null;
  }

  return (
    <div className={styles.carousel}>
      <button
        className={`${styles.control} ${styles.controlLeft}`}
        onClick={goToPrevious}
        disabled={!canGoPrevious}
        aria-label='Jogos anteriores'
      >
        ‹
      </button>

      <div className={styles.viewport}>
        <div
          className={styles.track}
          style={{
            transform: `translateX(calc(-${startIndex} * (25% + 0.75rem)))`,
          }}
        >
          {games.map(game => (
            <div className={styles.slide} key={game.id}>
              <GameCard game={game} />
            </div>
          ))}
        </div>
      </div>

      <button
        className={`${styles.control} ${styles.controlRight}`}
        onClick={goToNext}
        disabled={!canGoNext}
        aria-label='Próximos jogos'
      >
        ›
      </button>
    </div>
  );
}

export default GameCarousel;
