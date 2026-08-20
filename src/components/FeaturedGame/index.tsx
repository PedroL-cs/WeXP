import styles from './styles.module.css';
import type { Game } from '../../types/Game';

type FeaturedGameProps = {
  game: Game;
};

function FeaturedGame({ game }: FeaturedGameProps) {
  console.log(game); // Pro lint parar de reclamar
  return (
    // Tudo mockado!!!!
    <section className={styles.featuredGame}>
      <img
        className={styles.background}
        src='https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/730/library_hero.jpg'
        alt=''
      />
      <div className={styles.overlay}>
        <h1>Counter-Strike 2</h1>

        <p>
          Há mais de duas décadas, o Counter-Strike oferece uma experiência
          competitiva de elite moldada por milhões de jogadores mundialmente.
          Agora, o próximo capítulo da história do CS vai começar. Isso é
          Counter-Strike 2. Uma atualização gratuita para o CS:GO
        </p>
      </div>
    </section>
  );
}

export default FeaturedGame;
