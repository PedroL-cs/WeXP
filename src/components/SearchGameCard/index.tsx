import styles from './styles.module.css';
import { useNavigate } from 'react-router';
import {
  ArrowRightIcon,
  CalendarBlankIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import type { Game } from '../../types/Game';

interface SearchGameCardProps {
  game: Game;
  style?: React.CSSProperties;
}

function SearchGameCard({ game, style }: SearchGameCardProps) {
  const navigate = useNavigate();

  function handleClick() {
    navigate(`/games/${game.id}`);
  }

  return (
    <article
      className={styles.card}
      style={style}
      onClick={handleClick}
      role='button'
      tabIndex={0}
      onKeyDown={event => {
        if (event.key === 'Enter' || event.key === ' ') {
          handleClick();
        }
      }}
    >
      <div className={styles.imageContainer}>
        {game.images.header && (
          <img
            src={game.images.header}
            alt={game.name}
            className={styles.image}
            loading='lazy'
            onError={event => (event.currentTarget.hidden = true)}
          />
        )}
      </div>

      <div className={styles.content}>
        <h3 className={styles.title}>{game.name}</h3>

        {game.shortDescription && (
          <p className={styles.description}>{game.shortDescription}</p>
        )}

        <div className={styles.meta}>
          {game.achievements?.length > 0 && (
            <span className={styles.achievements}>
              <TrophyIcon size={14} weight='fill' />
              {game.achievements.length}
            </span>
          )}

          <span className={styles.releaseDate}>
            <CalendarBlankIcon size={14} />
            {new Date(game.releaseDate).toLocaleDateString('pt-BR')}
          </span>

          {game.genres?.length > 0 && (
            <div className={styles.genres}>
              {game.genres.slice(0, 3).map(genre => (
                <span key={genre.id}>{genre.name}</span>
              ))}
            </div>
          )}
        </div>
      </div>

      <span className={styles.arrow} aria-hidden='true'>
        <ArrowRightIcon size={20} weight='bold' />
      </span>
    </article>
  );
}

export default SearchGameCard;
