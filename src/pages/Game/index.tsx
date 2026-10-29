import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';

import styles from './styles.module.css';

import AchievementCard from '../../components/AchievementCard';
import {
  ArrowsDownUpIcon,
  CalendarBlankIcon,
  EyeIcon,
  EyeSlashIcon,
  LockSimpleIcon,
  MagnifyingGlassIcon,
  TrophyIcon,
} from '@phosphor-icons/react';

import { getGame, getGameIdBySteamAppId } from '../../api/games';

import type { Game } from '../../types/Game';
import SaveButton from '../../components/SaveButton';

type AchievementSort =
  | 'default'
  | 'name-asc'
  | 'name-desc'
  | 'rarity-asc'
  | 'rarity-desc';

export function SteamGameLoadingPage() {
  const { steamAppId } = useParams();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const appId = Number(steamAppId);
    if (!Number.isSafeInteger(appId) || appId <= 0) {
      setError('Este jogo possui um identificador inválido.');
      return;
    }

    const controller = new AbortController();
    setError('');

    getGameIdBySteamAppId(appId, controller.signal)
      .then(data => {
        if (!controller.signal.aborted) {
          navigate(`/games/${data.gameId}`, { replace: true });
        }
      })
      .catch(error => {
        if (error instanceof DOMException && error.name === 'AbortError') return;
        console.error('Erro ao preparar jogo:', error);
        setError(
          error instanceof Error
            ? error.message
            : 'Não foi possível preparar este jogo.',
        );
      });

    return () => controller.abort();
  }, [attempt, navigate, steamAppId]);

  if (error) {
    return (
      <main className={styles.gamePage}>
        <div className={styles.loadError}>
          <TrophyIcon size={30} weight='duotone' />
          <strong>Não foi possível carregar o jogo</strong>
          <p>{error}</p>
          <div className={styles.loadActions}>
            <button type='button' onClick={() => setAttempt(value => value + 1)}>
              Tentar novamente
            </button>
            <button type='button' onClick={() => navigate(-1)}>
              Voltar
            </button>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className={`${styles.gamePage} ${styles.initialLoad}`} aria-busy='true'>
      <div className={styles.heroSkeleton} />
      <div className={styles.loadingMessage} role='status'>
        <span className={styles.loadingSpinner} aria-hidden='true' />
        <div>
          <strong>Carregando dados do jogo</strong>
          <span>A primeira abertura pode levar alguns segundos.</span>
        </div>
      </div>
    </main>
  );
}

function GamePage() {
  const { id } = useParams();

  const [game, setGame] = useState<Game | null>(null);
  const [error, setError] = useState('');
  const [posterFailed, setPosterFailed] = useState(false);

  const [search, setSearch] = useState('');
  const [achievementSort, setAchievementSort] =
    useState<AchievementSort>('default');
  const [showSecretAchievements, setShowSecretAchievements] = useState(false);

  useEffect(() => {
    if (!id) return;

    setGame(null);
    setError('');
    setPosterFailed(false);

    getGame(id)
      .then(data => {
        setGame(data);
      })
      .catch(error => {
        console.error('Erro ao buscar jogo:', error);
        setError('Não foi possível carregar este jogo.');
      });
  }, [id]);

  if (error) {
    return (
      <main className={styles.gamePage}>
        <p className={styles.status}>{error}</p>
      </main>
    );
  }

  if (!game) {
    return (
      <main className={styles.gamePage} aria-busy='true'>
        <div className={styles.heroSkeleton} />
      </main>
    );
  }

  const filteredAchievements = game.achievements
    .filter(achievement =>
      achievement.name.toLowerCase().includes(search.toLowerCase()),
    )
    .sort((a, b) => {
      if (achievementSort === 'default') {
        return Number(a.hidden) - Number(b.hidden);
      }

      const nameOrder = a.name.localeCompare(b.name, 'pt-BR', {
        sensitivity: 'base',
      });

      if (achievementSort === 'name-asc') return nameOrder;
      if (achievementSort === 'name-desc') return -nameOrder;

      const aPercentage = a.globalUnlockPercentage;
      const bPercentage = b.globalUnlockPercentage;
      if (aPercentage == null && bPercentage == null) return nameOrder;
      if (aPercentage == null) return 1;
      if (bPercentage == null) return -1;

      const rarityOrder = aPercentage - bPercentage;
      if (rarityOrder === 0) return nameOrder;
      return achievementSort === 'rarity-asc'
        ? rarityOrder
        : -rarityOrder;
    });

  const secretCount = game.achievements.filter(
    achievement => achievement.hidden,
  ).length;

  return (
    <main className={styles.gamePage}>
      {/* Hero */}
      <section className={styles.gameHero}>
        {game.images.hero && (
          <img
            className={styles.heroImage}
            src={game.images.hero}
            alt=''
            aria-hidden='true'
            onError={event => (event.currentTarget.hidden = true)}
          />
        )}

        <div className={styles.heroOverlay} />

        <div className={styles.heroContent}>
          {!posterFailed && game.images.cover && (
            <img
              className={styles.poster}
              src={game.images.cover}
              alt={`Capa de ${game.name}`}
              onError={() => setPosterFailed(true)}
            />
          )}

          <div className={styles.gameInfo}>
            {game.genres?.length > 0 && (
              <ul className={styles.genres}>
                {game.genres.slice(0, 4).map(genre => (
                  <li key={genre.id}>{genre.name}</li>
                ))}
              </ul>
            )}

            <h1>{game.name}</h1>

            <p>{game.shortDescription}</p>

            <div className={styles.heroActions}>
              <dl className={styles.stats}>
                <div className={styles.statXp}>
                  <dt>
                    <TrophyIcon size={16} weight='fill' /> Conquistas
                  </dt>
                  <dd>{game.achievements.length}</dd>
                </div>

                {secretCount > 0 && (
                  <div>
                    <dt>
                      <LockSimpleIcon size={16} weight='fill' /> Secretas
                    </dt>
                    <dd>{secretCount}</dd>
                  </div>
                )}

                <div>
                  <dt>
                    <CalendarBlankIcon size={16} weight='fill' /> Lançamento
                  </dt>
                  <dd>
                    {new Date(game.releaseDate).toLocaleDateString('pt-BR')}
                  </dd>
                </div>
              </dl>
              <SaveButton type='GAME' resourceId={game.id} />
            </div>
          </div>
        </div>
      </section>

      {/* Conteúdo */}
      <div className={styles.gameContent}>
        {game.achievements.length > 0 ? (
          <section className={styles.achievementsSection}>
            <div className={styles.sectionHeader}>
              <div>
                <h2>Conquistas</h2>
                <p>Escolha uma conquista para ver o guia da comunidade.</p>
              </div>

              <span className={styles.achievementCount}>
                {filteredAchievements.length}
                <span>/{game.achievements.length}</span>
              </span>
            </div>

            <div className={styles.toolbar}>
              <label className={styles.searchContainer}>
                <MagnifyingGlassIcon size={18} />
                <input
                  type='search'
                  placeholder='Buscar conquista...'
                  aria-label='Buscar conquista'
                  value={search}
                  onChange={event => setSearch(event.target.value)}
                  className={styles.searchInput}
                />
              </label>

              <label className={styles.sortContainer}>
                <ArrowsDownUpIcon size={22} />
                <select
                  value={achievementSort}
                  onChange={event =>
                    setAchievementSort(event.target.value as AchievementSort)
                  }
                  aria-label='Ordenar conquistas'
                >
                  <option value='default'>Padrão</option>
                  <option value='name-asc'>Nome (A–Z)</option>
                  <option value='name-desc'>Nome (Z–A)</option>
                  <option value='rarity-asc'>Mais raras</option>
                  <option value='rarity-desc'>Mais comuns</option>
                </select>
              </label>

              {secretCount > 0 && (
                <button
                  type='button'
                  className={`${styles.secretToggle} ${
                    showSecretAchievements ? styles.on : ''
                  }`}
                  aria-pressed={showSecretAchievements}
                  onClick={() => setShowSecretAchievements(prev => !prev)}
                >
                  {showSecretAchievements ? (
                    <EyeIcon size={22} />
                  ) : (
                    <EyeSlashIcon size={22} />
                  )}
                  {showSecretAchievements
                    ? 'Ocultar secretas'
                    : 'Revelar secretas'}
                </button>
              )}
            </div>

            {filteredAchievements.length > 0 ? (
              <div className={styles.achievementsList}>
                {filteredAchievements.map((achievement, index) => (
                  <AchievementCard
                    key={achievement.id}
                    achievement={achievement}
                    showSecret={showSecretAchievements}
                    gameId={game.id}
                    style={{ animationDelay: `${Math.min(index, 12) * 30}ms` }}
                  />
                ))}
              </div>
            ) : (
              <p className={styles.status}>
                Nenhuma conquista encontrada para "{search}".
              </p>
            )}
          </section>
        ) : (
          <section className={styles.noAchievements}>
            <TrophyIcon size={40} weight='duotone' />
            <h3>Este jogo não possui conquistas</h3>
            <p>Não encontramos conquistas disponíveis para este jogo.</p>
          </section>
        )}
      </div>
    </main>
  );
}

export default GamePage;
