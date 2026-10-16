import styles from './styles.module.css';
import type { Game } from '../../types/Game';
import { TrophyIcon } from '@phosphor-icons/react';
import { useNavigate } from 'react-router';

export type GameCardProps = {
  game: Game;
};

function GameCard({ game }: GameCardProps) {
  const navigate = useNavigate();

  function handleClick() {
    navigate(`/games/${game.id}`);
  }

  const genre = game.genres?.[0]?.name;

  return (
    <div
      className={styles.gameCard}
      onClick={handleClick}
      role='link'
      tabIndex={0}
      onKeyDown={event => {
        if (event.key === 'Enter') handleClick();
      }}
    >
      <div className={styles.imageContainer}>
        {game.images.header && (
          <img
            src={game.images.header}
            alt={game.name}
            loading='lazy'
            onError={event => (event.currentTarget.hidden = true)}
          />
        )}
      </div>

      <div className={styles.gameCardContent}>
        <h2>{game.name}</h2>

        <div className={styles.meta}>
          <p className={styles.achievementsInfo}>
            <TrophyIcon size={14} weight='fill' />
            <span>{game.achievements.length}</span>
          </p>

          {genre && <span className={styles.genre}>{genre}</span>}
        </div>
      </div>
    </div>
  );
}

export default GameCard;
