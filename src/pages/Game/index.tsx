import { useState } from 'react';

import styles from './styles.module.css';

import { mockGame } from '../../mocks/gameMocks';
import { gameAchievements } from '../../mocks/gameAchievements';

import AchievementCard from '../../components/AchievementCard';
import { EyeIcon, EyeSlashIcon } from '@phosphor-icons/react';

function GamePage() {
  const game = mockGame;

  const [search, setSearch] = useState('');

  const [showSecretAchievements, setShowSecretAchievements] = useState(false);

  const filteredAchievements = gameAchievements['RE9']
    .filter(achievement =>
      achievement.name.toLowerCase().includes(search.toLowerCase()),
    )
    .sort((a, b) => Number(a.is_hidden) - Number(b.is_hidden));

  return (
    <main className={styles.gamePage}>
      {/* Hero */}
      <section className={styles.gameHero}>
        <img
          className={styles.heroImage}
          src={game.images.hero}
          alt={game.name}
        />

        <div className={styles.heroOverlay} />

        <div className={styles.heroContent}>
          {/* <img
            className={styles.gameCapsule}
            src={game.images.capsule}
            alt={game.name}
          /> */}

          <div className={styles.gameInfo}>
            <h1>{game.name}</h1>

            <p>{game.shortDescription}</p>

            <span>Lançamento: {game.releaseDate}</span>
          </div>
        </div>
      </section>

      {/* Conteúdo */}
      <div className={styles.gameContent}>
        <section className={styles.achievementsSection}>
          <div className={styles.sectionHeader}>
            <div>
              <h2>Conquistas</h2>
              <p>Explore as conquistas e descubra como desbloqueá-las.</p>
            </div>

            <span className={styles.achievementCount}>
              {filteredAchievements.length} conquistas
            </span>
          </div>

          {/* Filtro */}
          <div className={styles.searchContainer}>
            <input
              type='text'
              placeholder='Buscar conquista...'
              value={search}
              onChange={event => setSearch(event.target.value)}
              className={styles.searchInput}
            />
          </div>

          {/* Lista */}
          <div className={styles.achievementsList}>
            {filteredAchievements.map(achievement => (
              <AchievementCard
                key={achievement.id}
                achievement={achievement}
                showSecret={showSecretAchievements}
              />
            ))}
          </div>
        </section>
      </div>
      <button
        className={styles.secretToggle}
        title='Alternar visibilidade de conquistas secretas'
        onClick={() => setShowSecretAchievements(prev => !prev)}
      >
        {showSecretAchievements ? (
          <EyeIcon size={24} />
        ) : (
          <EyeSlashIcon size={24} />
        )}
      </button>
    </main>
  );
}

export default GamePage;
