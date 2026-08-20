import styles from './styles.module.css';
import type { Game } from '../../types/Game';
import { MedalIcon } from '@phosphor-icons/react';

export type GameCardProps = {
  game: Game;
};

function GameCard({ game }: GameCardProps) {
  return (
    <div className={styles.gameCard}>
      <img src={game.largeCapsuleImage} alt={game.name} />
      <div className={styles.gameCardContent}>
        <h2>{game.name}</h2>

        <p className={styles.achievementsInfo}>
          <span>{game.achievements}</span>
          <MedalIcon size={13} weight='fill' />
        </p>
        {/* {game.discountPercent > 0 && game.originalPrice !== null && (
          <div className={styles.discount}>
            <span className={styles.discountPercent}>
              -{game.discountPercent}%
            </span>

            <span className={styles.originalPrice}>
              R$ {(game.originalPrice / 100).toFixed(2)}
            </span>
          </div>
        )}

        <p className={styles.price}>R$ {(game.finalPrice / 100).toFixed(2)}</p> */}
      </div>
    </div>
  );
}

export default GameCard;
