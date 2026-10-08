import { PaperPlaneTiltIcon, NotePencilIcon } from '@phosphor-icons/react';
import { useEffect, useRef, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router';
import { getAchievement } from '../../api/achievements';
import { createGuideRevision, getAchievementGuide } from '../../api/guides';
import GuideEditor from '../../components/GuideEditor';
import type { Guide } from '../../types/Guide';
import { useAuth } from '../../contexts/AuthContext';
import styles from './styles.module.css';

function CreateRevisionPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { gameId, achievementId } = useParams();
  const content = useRef('');
  const [guide, setGuide] = useState<Guide | null>(null);
  const [achievementName, setAchievementName] = useState('');
  const [summary, setSummary] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    async function load() {
      if (!achievementId) {
        setError('Conquista não encontrada.');
        setLoading(false);
        return;
      }
      try {
        const [item, achievement] = await Promise.all([
          getAchievementGuide(achievementId),
          getAchievement(achievementId).catch(() => null),
        ]);

        setAchievementName(achievement?.name ?? '');

        if (!item) {
          setError('Essa conquista ainda não possui um guia.');
          return;
        }

        content.current = item.content;
        setGuide(item);
      } catch {
        setError('Não foi possível carregar o guia para revisão.');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [achievementId]);

  if (!isAuthenticated) {
    return <Navigate to='/login' replace />;
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!guide?.id || !summary.trim() || !content.current.trim()) {
      setError('Informe o conteúdo revisado e um resumo da alteração.');
      return;
    }
    try {
      setError('');
      setSubmitting(true);
      await createGuideRevision(guide.id, content.current, summary.trim());
      navigate(
        `/games/${gameId}/achievements/${achievementId}/guide?tab=revisions`,
      );
    } catch {
      setError('Não foi possível enviar a revisão. Tente novamente.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <div className={styles.iconFrame}>
          <NotePencilIcon size={28} weight='duotone' />
        </div>
        <div>
          <p className={styles.eyebrow}>{achievementName || 'Conquista'}</p>
          <h1 className={styles.title}>Sugerir revisão</h1>
          <span className={styles.description}>
            Edite o guia e descreva brevemente o que foi alterado.
          </span>
        </div>
      </header>
      {loading && <div className={styles.status}>Carregando guia...</div>}
      {!loading && !guide && <div className={styles.status}>{error}</div>}
      {!loading && guide && (
        <form className={styles.form} onSubmit={submit}>
          <label className={styles.summaryLabel} htmlFor='summary'>
            Resumo da alteração
          </label>
          <input
            className={styles.summaryInput}
            id='summary'
            value={summary}
            onChange={event => setSummary(event.target.value)}
            maxLength={255}
            required
            placeholder='Ex.: Corrige a ordem dos passos finais'
          />
          <GuideEditor
            initialMarkdown={guide.content}
            onChange={value => {
              content.current = value;
            }}
          />
          <footer className={styles.footer}>
            <div aria-live='polite'>
              {error && <span className={styles.error}>{error}</span>}
            </div>
            <button
              className={styles.submitButton}
              type='submit'
              disabled={submitting}
            >
              <PaperPlaneTiltIcon size={18} weight='fill' />
              {submitting ? 'Enviando...' : 'Enviar revisão'}
            </button>
          </footer>
        </form>
      )}
    </main>
  );
}

export default CreateRevisionPage;
