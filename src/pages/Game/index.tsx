import { useEffect, useState } from 'react';
import { useParams } from 'react-router';

import styles from './styles.module.css';

import AchievementCard from '../../components/AchievementCard';
import { EyeIcon, EyeSlashIcon } from '@phosphor-icons/react';

import { getGame } from '../../api/games';

import type { Game } from '../../types/Game';

function GamePage() {
  const { id } = useParams();

  const [game, setGame] = useState<Game | null>(null);

  const [search, setSearch] = useState('');
  const [showSecretAchievements, setShowSecretAchievements] = useState(false);

  useEffect(() => {
    if (!id) {
      console.log('GamePage - sem ID');
      return;
    }

    console.log('Buscando jogo:', id);

    getGame(id)
      .then(data => {
        console.log('Jogo recebido:', data);
        setGame(data);
      })
      .catch(error => {
        console.error('Erro ao buscar jogo:', error);
      });
  }, [id]);

  if (!game) {
    return <div>Carregando...</div>;
  }

  const filteredAchievements = game.achievements.items
    .filter(achievement =>
      achievement.name.toLowerCase().includes(search.toLowerCase()),
    )
    .sort((a, b) => Number(a.hidden) - Number(b.hidden));

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
          <div className={styles.gameInfo}>
            <h1>{game.name}</h1>

            <p>{game.shortDescription}</p>

            <span>
              Lançamento:{' '}
              {new Date(game.releaseDate).toLocaleDateString('pt-BR')}
            </span>
          </div>
        </div>
      </section>

      {/* Conteúdo */}
      <div className={styles.gameContent}>
        {game.achievements.total > 0 ? (
          <>
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

              <div className={styles.searchContainer}>
                <input
                  type='text'
                  placeholder='Buscar conquista...'
                  value={search}
                  onChange={event => setSearch(event.target.value)}
                  className={styles.searchInput}
                />
              </div>

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
          </>
        ) : (
          <section className={styles.noAchievements}>
            <h3>Este jogo não possui conquistas</h3>
            <p>Não encontramos conquistas disponíveis para este jogo.</p>
          </section>
        )}
      </div>

      {game.achievements.total > 0 && (
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
      )}
    </main>
  );
}

export default GamePage;
