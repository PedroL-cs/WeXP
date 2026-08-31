import styles from './styles.module.css';
import type { Game } from '../../types/Game';
import { MedalIcon } from '@phosphor-icons/react';
import { useNavigate } from 'react-router';

export type GameCardProps = {
  game: Game;
};

function GameCard({ game }: GameCardProps) {
  const navigate = useNavigate();

  function handleClick() {
    navigate(`/games/${game.id}`);
  }

  return (
    <div className={styles.gameCard} onClick={handleClick}>
      <img src={game.images.header} alt={game.name} />
      <div className={styles.gameCardContent}>
        <h2>{game.name}</h2>

        <p className={styles.achievementsInfo}>
          <span>{game.achievements.total}</span>
          <MedalIcon size={13} weight='fill' />
        </p>
      </div>
    </div>
  );
}

export default GameCard;
