import styles from './styles.module.css';
import { useNavigate } from 'react-router';
import type { Game } from '../../types/Game';

interface SearchGameCardProps {
  game: Game;
}

function SearchGameCard({ game }: SearchGameCardProps) {
  const navigate = useNavigate();

  function handleClick() {
    navigate(`/games/${game.id}`);
  }

  let fixedDate =
    game.releaseDate != null ? game.releaseDate.slice(0, 10) : null;

  return (
    <article
      className={styles.card}
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
        <img
          src={game.images.header}
          alt={game.name}
          className={styles.image}
        />
      </div>

      <div className={styles.content}>
        <h3 className={styles.title}>{game.name}</h3>

        {game.shortDescription && (
          <p className={styles.description}>{game.shortDescription}</p>
        )}

        <span className={styles.releaseDate}>{fixedDate}</span>
      </div>
    </article>
  );
}

export default SearchGameCard;
