import { useCallback, useEffect, useRef, useState } from 'react';
import { CaretLeftIcon, CaretRightIcon } from '@phosphor-icons/react';
import GameCard from '../GameCard';
import type { Game } from '../../types/Game';
import styles from './styles.module.css';

type GameCarouselProps = {
  games: Game[];
};

function GameCarousel({ games }: GameCarouselProps) {
  const trackRef = useRef<HTMLDivElement>(null);

  const [canGoPrevious, setCanGoPrevious] = useState(false);
  const [canGoNext, setCanGoNext] = useState(false);

  // Habilita/desabilita as setas conforme a posição do scroll
  const updateControls = useCallback(() => {
    const track = trackRef.current;
    if (!track) return;

    setCanGoPrevious(track.scrollLeft > 4);
    setCanGoNext(track.scrollLeft + track.clientWidth < track.scrollWidth - 4);
  }, []);

  useEffect(() => {
    updateControls();
    window.addEventListener('resize', updateControls);
    return () => window.removeEventListener('resize', updateControls);
  }, [games.length, updateControls]);

  const scrollByPage = (direction: 1 | -1) => {
    const track = trackRef.current;
    if (!track) return;

    track.scrollBy({
      left: direction * track.clientWidth * 0.8,
      behavior: 'smooth',
    });
  };

  if (games.length === 0) {
    return null;
  }

  return (
    <div className={styles.carousel}>
      <div
        className={`${styles.viewport} ${canGoPrevious ? styles.fadeLeft : ''} ${
          canGoNext ? styles.fadeRight : ''
        }`}
      >
        <div className={styles.track} ref={trackRef} onScroll={updateControls}>
          {games.map(game => (
            <div className={styles.slide} key={game.id}>
              <GameCard game={game} />
            </div>
          ))}
        </div>
      </div>

      <div className={styles.controls}>
        <button
          className={styles.control}
          onClick={() => scrollByPage(-1)}
          disabled={!canGoPrevious}
          aria-label='Jogos anteriores'
        >
          <CaretLeftIcon size={18} weight='bold' />
        </button>

        <button
          className={styles.control}
          onClick={() => scrollByPage(1)}
          disabled={!canGoNext}
          aria-label='Próximos jogos'
        >
          <CaretRightIcon size={18} weight='bold' />
        </button>
      </div>
    </div>
  );
}

export default GameCarousel;
