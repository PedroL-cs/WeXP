import {
  ArrowLeftIcon,
  ClockCounterClockwiseIcon,
  NotePencilIcon,
} from '@phosphor-icons/react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router';
import { getAchievement } from '../../api/achievements';
import { getAchievementGuide, getGuideRevisions } from '../../api/guides';
import GuideContent from '../../components/GuideContent';
import GuideDiff from '../../components/GuideDiff';
import { useAuth } from '../../contexts/AuthContext';
import type { Guide, GuideRevision } from '../../types/Guide';
import styles from './styles.module.css';

function GuidePage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const { gameId, achievementId } = useParams();
  const [params, setParams] = useSearchParams();
  const [guide, setGuide] = useState<Guide | null>(null);
  const [achievementName, setAchievementName] = useState('');
  const [revisions, setRevisions] = useState<GuideRevision[]>([]);
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
      <button
        type='button'
        className={styles.back}
        onClick={() => navigate(-1)}
      >
        <ArrowLeftIcon size={20} />
        Voltar
      </button>
      <header className={styles.header}>
        <div>
          <p>{achievementName || 'Conquista'}</p>
          <h1>Guia da conquista</h1>
        </div>
        {isAuthenticated && guide?.id && (
          <button
            type='button'
            className={styles.outline}
            onClick={() =>
              navigate(`/achievements/${achievementId}/guide/revise`)
            }
          >
            <NotePencilIcon size={20} />
            Sugerir revisão
          </button>
        )}
      </header>
      {loading && <p className={styles.status}>Carregando guia...</p>}
      {!loading && error && <p className={styles.status}>{error}</p>}
      {!loading && !error && !guide?.content && (
        <section className={styles.emptyState}>
          <h2>Esta conquista ainda não possui um guia</h2>

          <p>
            Seja o primeiro a compartilhar um passo a passo para ajudar outros
            jogadores.
          </p>

          {isAuthenticated && (
            <button
              type='button'
              className={styles.primaryButton}
              onClick={() =>
                navigate(
                  `/games/${gameId}/achievements/${achievementId}/guide/create`,
                )
              }
            >
              Criar primeiro guia
            </button>
          )}
        </section>
      )}
      {!loading && !error && guide?.content && (
        <>
          <div className={styles.tabs}>
            <button
              type='button'
              className={tab === 'guide' ? styles.active : ''}
              onClick={() => setParams({})}
            >
              Guia atual
            </button>
            <button
              type='button'
              className={tab === 'revisions' ? styles.active : ''}
              onClick={() => setParams({ tab: 'revisions' })}
            >
              <ClockCounterClockwiseIcon size={18} />
              Revisões
            </button>
          </div>
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
                    {revisions.map(revision => (
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
                        <span>
                          {revision.status === 'PENDING'
                            ? 'Pendente'
                            : revision.status || 'Sem status'}
                        </span>
                      </button>
                    ))}
                  </div>
                  {selected && (
                    <div>
                      <h2>Diferenças propostas</h2>
                      <p className={styles.note}>
                        Vermelho indica remoção; verde indica adição.
                      </p>
                      <GuideDiff
                        previous={guide.content}
                        current={selected.content}
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
