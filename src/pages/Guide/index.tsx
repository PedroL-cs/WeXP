import {
  BookOpenTextIcon,
  ClockCounterClockwiseIcon,
  NotePencilIcon,
  PlusIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router';
import { getAchievement } from '../../api/achievements';
import { getAchievementGuide, getGuideRevisions } from '../../api/guides';
import { findUserById } from '../../api/users';
import GuideContent from '../../components/GuideContent';
import GuideDiff from '../../components/GuideDiff';
import { useAuth } from '../../contexts/AuthContext';
import type { Guide, GuideRevision } from '../../types/Guide';
import styles from './styles.module.css';

const statusLabels: Record<string, string> = {
  PENDING: 'Pendente',
  APPROVED: 'Aprovada',
  REJECTED: 'Rejeitada',
};

function GuidePage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const { gameId, achievementId } = useParams();
  const [params, setParams] = useSearchParams();
  const [guide, setGuide] = useState<Guide | null>(null);
  const [achievementName, setAchievementName] = useState('');
  const [achievementDescription, setAchievementDescription] = useState('');
  const [achievementIcon, setAchievementIcon] = useState('');
  const [revisions, setRevisions] = useState<GuideRevision[]>([]);
  const [revisionAuthors, setRevisionAuthors] = useState<
    Record<string, { username: string; avatar?: string }>
  >({});
  const [selected, setSelected] = useState<GuideRevision | null>(null);
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [error, setError] = useState('');
  const tab = params.get('tab') === 'revisions' ? 'revisions' : 'guide';

  useEffect(() => {
    async function load() {
      if (!achievementId) {
        setError('Conquista não encontrada.');
        setLoading(false);
        return;
      }
      try {
        const [achievement, currentGuide] = await Promise.all([
          getAchievement(achievementId),
          getAchievementGuide(achievementId),
        ]);
        setAchievementName(achievement.name);
        setAchievementDescription(achievement.description ?? '');
        setAchievementIcon(achievement.icon_url ?? achievement.icon ?? '');

        setGuide(currentGuide);
      } catch {
        setError('Não foi possível carregar o guia.');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [achievementId]);

  useEffect(() => {
    async function load() {
      if (tab !== 'revisions' || !guide?.id) return;
      try {
        setHistoryLoading(true);
        const items = await getGuideRevisions(guide.id);
        setRevisions(items);
        setSelected(items[0] ?? null);

        const authors = await Promise.all(
          items.map(async revision => {
            if (!revision.authorPublicId) return null;

            try {
              const author = await findUserById(revision.authorPublicId);

              return [
                revision.publicId,
                {
                  username: author.username,
                  avatar: author.avatar,
                },
              ] as [string, { username: string; avatar?: string }];
            } catch {
              return null;
            }
          }),
        );

        const validAuthors = authors.filter(
          (author): author is [string, { username: string; avatar?: string }] =>
            author !== null,
        );

        setRevisionAuthors(Object.fromEntries(validAuthors));
      } catch {
        setError('Não foi possível carregar o histórico de revisões.');
      } finally {
        setHistoryLoading(false);
      }
    }
    load();
  }, [tab, guide?.id]);

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        {/* O próprio ícone da conquista, borrado, vira a luz ambiente */}
        {achievementIcon && (
          <div
            className={styles.ambient}
            style={{ backgroundImage: `url(${achievementIcon})` }}
            aria-hidden='true'
          />
        )}

        <div className={styles.iconFrame}>
          {achievementIcon ? (
            <img
              className={styles.achievementIcon}
              src={achievementIcon}
              alt={`Ícone da conquista ${achievementName}`}
            />
          ) : (
            <TrophyIcon size={36} weight='duotone' />
          )}
        </div>

        <div className={styles.headerInfo}>
          <p className={styles.eyebrow}>
            <TrophyIcon size={14} weight='fill' />
            Guia da conquista
          </p>
          <h1>{achievementName || (loading ? '\u00a0' : 'Conquista')}</h1>
          {achievementDescription && (
            <p className={styles.achievementDescription}>
              {achievementDescription}
            </p>
          )}
          {guide?.author && (
            <span className={styles.author}>
              Escrito por <strong>{guide.author}</strong>
            </span>
          )}
        </div>

        {isAuthenticated && guide?.id && (
          <button
            type='button'
            className={styles.outline}
            onClick={() =>
              navigate(
                `/games/${gameId}/achievements/${achievementId}/guide/revise`,
              )
            }
          >
            <NotePencilIcon size={18} />
            Sugerir revisão
          </button>
        )}
      </header>
      {loading && <p className={styles.status}>Carregando guia...</p>}
      {!loading && error && <p className={styles.status}>{error}</p>}
      {!loading && !error && !guide?.content && (
        <section className={styles.emptyState}>
          <div className={styles.emptyIcon}>
            <BookOpenTextIcon size={36} weight='duotone' />
          </div>

          <h2>Esta conquista ainda não possui um guia</h2>

          <p>
            Seja o primeiro a compartilhar um passo a passo para ajudar outros
            jogadores.
          </p>

          {isAuthenticated ? (
            <button
              type='button'
              className={styles.primaryButton}
              onClick={() =>
                navigate(
                  `/games/${gameId}/achievements/${achievementId}/guide/create`,
                )
              }
            >
              <PlusIcon size={18} weight='bold' />
              Criar primeiro guia
            </button>
          ) : (
            <Link to='/login' className={styles.primaryButton}>
              Entre para escrever o primeiro guia
            </Link>
          )}
        </section>
      )}
      {!loading && !error && guide?.content && (
        <>
          {isAuthenticated && (
            <div className={styles.tabs}>
              <button
                type='button'
                className={tab === 'guide' ? styles.active : ''}
                onClick={() => setParams({})}
              >
                <BookOpenTextIcon size={16} />
                Guia atual
              </button>
              <button
                type='button'
                className={tab === 'revisions' ? styles.active : ''}
                onClick={() => setParams({ tab: 'revisions' })}
              >
                <ClockCounterClockwiseIcon size={16} />
                Revisões
              </button>
            </div>
          )}
          {tab === 'guide' && <GuideContent markdown={guide.content} />}
          {tab === 'revisions' && (
            <section className={styles.history}>
              {historyLoading && (
                <p className={styles.status}>Carregando revisões...</p>
              )}
              {!historyLoading && revisions.length === 0 && (
                <p className={styles.status}>
                  Ainda não há revisões sugeridas.
                </p>
              )}
              {revisions.length > 0 && (
                <div className={styles.grid}>
                  <div className={styles.list}>
                    {revisions.map(revision => {
                      const revisionAuthor = revisionAuthors[revision.publicId];

                      return (
                        <button
                          type='button'
                          key={revision.publicId}
                          className={
                            selected?.publicId === revision.publicId
                              ? styles.selected
                              : ''
                          }
                          onClick={() => setSelected(revision)}
                        >
                          <strong>
                            {revision.changeSummary || 'Alteração sugerida'}
                          </strong>
                          <span
                            className={`${styles.badge} ${
                              styles[`badge${revision.status ?? ''}`] ?? ''
                            }`}
                          >
                            {statusLabels[revision.status ?? ''] ??
                              revision.status ??
                              'Sem status'}
                          </span>
                          {revisionAuthor && (
                            <div className={styles.authorRow}>
                              <span>Por {revisionAuthor.username}</span>

                              {revisionAuthor.avatar && (
                                <img
                                  src={revisionAuthor.avatar}
                                  alt={`Foto de ${revisionAuthor.username}`}
                                  className={styles.revisionAvatar}
                                />
                              )}
                            </div>
                          )}
                        </button>
                      );
                    })}
                  </div>
                  {selected && (
                    <div className={styles.diffPanel}>
                      <h2>Diferenças propostas</h2>
                      <p className={styles.note}>
                        <span className={styles.legendRemoved}>Removido</span>
                        <span className={styles.legendChanged}>Alterado</span>
                        <span className={styles.legendAdded}>Adicionado</span>
                      </p>
                      <GuideDiff
                        previous={guide.content}
                        current={selected.content}
                        previousAuthor={{
                          username: guide.author || 'Autor original',
                        }}
                        currentAuthor={
                          revisionAuthors[selected.publicId] ?? {
                            username: 'Autor da revisão',
                          }
                        }
                      />
                    </div>
                  )}
                </div>
              )}
            </section>
          )}
        </>
      )}
    </main>
  );
}

export default GuidePage;
