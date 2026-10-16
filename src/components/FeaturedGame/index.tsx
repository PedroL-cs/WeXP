import { useState } from 'react';
import {
  ArrowRightIcon,
  CaretLeftIcon,
  CaretRightIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import styles from './styles.module.css';
import type { Game } from '../../types/Game';
import { useNavigate } from 'react-router';

type FeaturedGameProps = {
  games: Game[];
};

function FeaturedGame({ games }: FeaturedGameProps) {
  const navigate = useNavigate();

  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [brokenLogos, setBrokenLogos] = useState<string[]>([]);

  const currentGame = games[currentIndex];

  const goToPrevious = () => {
    setCurrentIndex(current =>
      current === 0 ? games.length - 1 : current - 1,
    );
  };

  const goToNext = () => {
    setCurrentIndex(current => (current + 1) % games.length);
  };

  const goToGame = () => {
    navigate(`/games/${currentGame.id}`);
  };

  if (!currentGame) {
    return null;
  }

  const showLogo =
    currentGame.images.logo && !brokenLogos.includes(currentGame.id);

  return (
    <section
      className={`${styles.featuredGame} ${isPaused ? styles.paused : ''}`}
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
      aria-roledescription='carrossel'
    >
      {currentGame.images.hero && (
        <img
          key={currentGame.id}
          className={styles.background}
          src={currentGame.images.hero}
          onClick={goToGame}
          onError={event => (event.currentTarget.hidden = true)}
          alt=''
        />
      )}

      <div className={styles.overlay} key={`info-${currentGame.id}`}>
        <span className={styles.eyebrow}>
          Em destaque
          <span className={styles.counter}>
            {String(currentIndex + 1).padStart(2, '0')}
            <span>/{String(games.length).padStart(2, '0')}</span>
          </span>
        </span>

        {showLogo ? (
          <img
            className={styles.logo}
            src={currentGame.images.logo ?? undefined}
            alt={currentGame.name}
            onError={() =>
              setBrokenLogos(current => [...current, currentGame.id])
            }
          />
        ) : (
          <h1>{currentGame.name}</h1>
        )}

        <p>{currentGame.shortDescription}</p>

        <div className={styles.actions}>
          <button type='button' className={styles.cta} onClick={goToGame}>
            Ver conquistas
            <ArrowRightIcon size={18} weight='bold' />
          </button>

          {currentGame.achievements.length > 0 && (
            <span className={styles.achievements}>
              <TrophyIcon size={16} weight='fill' />
              {currentGame.achievements.length} conquistas
            </span>
          )}
        </div>
      </div>

      {games.length > 1 && (
        <>
          <div className={styles.controls}>
            <button
              className={styles.control}
              onClick={goToPrevious}
              aria-label='Jogo anterior'
            >
              <CaretLeftIcon size={20} weight='bold' />
            </button>

            <button
              className={styles.control}
              onClick={goToNext}
              aria-label='Próximo jogo'
            >
              <CaretRightIcon size={20} weight='bold' />
            </button>
          </div>

          <div className={styles.dots}>
            {games.map((game, index) => (
              <button
                key={game.id}
                className={`${styles.dot} ${
                  index === currentIndex ? styles.active : ''
                } ${index < currentIndex ? styles.seen : ''}`}
                onClick={() => setCurrentIndex(index)}
                aria-label={`Mostrar ${game.name}`}
              >
                {/* A barra ativa avança o carrossel ao terminar de encher */}
                {index === currentIndex && (
                  <span
                    key={currentIndex}
                    className={styles.fill}
                    onAnimationEnd={goToNext}
                  />
                )}
              </button>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

export default FeaturedGame;
