import GameCard from '../../components/GameCard';
import styles from './styles.module.css';
import { featuredGames } from '../../mocks/featuredGames';
import FeaturedGame from '../../components/FeaturedGame';

export function HomePage() {
  return (
    <>
      <section className={styles.homePage}>
        <h2>Destaques</h2>
        <FeaturedGame game={featuredGames.topSellers[0]} />

        <h2>Mais vendidos</h2>

        <div className={styles.gameGrid}>
          {featuredGames.topSellers.map(game => (
            <GameCard key={game.id} game={game} />
          ))}
        </div>

        <h2>Novos lançamentos</h2>

        <div className={styles.gameGrid}>
          {featuredGames.newReleases.map(game => (
            <GameCard key={game.id} game={game} />
          ))}
        </div>

        <h2>Especiais</h2>

        <div className={styles.gameGrid}>
          {featuredGames.specials.map(game => (
            <GameCard key={game.id} game={game} />
          ))}
        </div>
      </section>
    </>
  );
}
