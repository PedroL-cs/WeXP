import { PaperPlaneTiltIcon, NotePencilIcon } from '@phosphor-icons/react';
import { useEffect, useRef, useState } from 'react';
import { Navigate, useLocation, useNavigate, useParams } from 'react-router';
import { getAchievement } from '../../api/achievements';
import { createGuideRevision, getAchievementGuide } from '../../api/guides';
import GuideEditor from '../../components/GuideEditor';
import type { Guide } from '../../types/Guide';
import { useAuth } from '../../contexts/AuthContext';
import styles from './styles.module.css';

function CreateRevisionPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const { gameId, achievementId } = useParams();
  const content = useRef('');
  const [guide, setGuide] = useState<Guide | null>(null);
  const [achievementName, setAchievementName] = useState('');
  const [summary, setSummary] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const submitRequest = useRef<AbortController | null>(null);

  useEffect(() => {
    if (!isAuthenticated) return;
    const controller = new AbortController();
    async function load() {
      if (!achievementId) {
        setError('Conquista não encontrada.');
        setLoading(false);
        return;
      }
      setLoading(true);
      setGuide(null);
      setError('');
      setSummary('');
      setSubmitting(false);
      submitRequest.current?.abort();
      submitRequest.current = null;
      try {
        const [item, achievement] = await Promise.all([
          getAchievementGuide(achievementId, controller.signal),
          getAchievement(achievementId, controller.signal).catch(() => null),
        ]);
        if (controller.signal.aborted) return;
        setAchievementName(achievement?.name ?? '');

        if (!item?.activeRevisionId) {
          setError(
            item
              ? 'A versão principal ainda não está disponível para edição. Tente novamente.'
              : 'Essa conquista ainda não possui um guia.',
          );
          return;
        }

        content.current = item.content;
        setGuide(item);
      } catch (error) {
        if (!controller.signal.aborted)
          setError(
            error instanceof Error
              ? error.message
              : 'Não foi possível carregar o guia para revisão.',
          );
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }
    load();
    return () => {
      controller.abort();
      submitRequest.current?.abort();
    };
  }, [achievementId, isAuthenticated]);

  useEffect(() => () => submitRequest.current?.abort(), []);

  if (!isAuthenticated) {
    return <Navigate to='/login' state={{ from: location.pathname }} replace />;
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitRequest.current) return;
    if (
      !guide?.id ||
      !guide.activeRevisionId ||
      !summary.trim() ||
      !content.current.trim()
    ) {
      setError('Informe o conteúdo revisado e um resumo da alteração.');
      return;
    }
    if (content.current.length < 10 || content.current.length > 100000) {
      setError('O conteúdo deve ter entre 10 e 100000 caracteres.');
      return;
    }
    const controller = new AbortController();
    submitRequest.current = controller;
    try {
      setError('');
      setSubmitting(true);
      const revision = await createGuideRevision(
        guide.id,
        {
          baseRevisionId: guide.activeRevisionId,
          content: content.current,
          changeSummary: summary.trim(),
        },
        controller.signal,
      );
      if (controller.signal.aborted) return;
      const params = new URLSearchParams({
        tab: 'revisions',
        revision: revision.publicId,
      });
      navigate(
        `/games/${gameId}/achievements/${achievementId}/guide?${params}`,
        { state: { publishedRevisionId: revision.publicId } },
      );
    } catch (error) {
      if (!controller.signal.aborted)
        setError(
          error instanceof Error
            ? error.message
            : 'Não foi possível enviar a revisão. Tente novamente.',
        );
    } finally {
      if (submitRequest.current === controller) submitRequest.current = null;
      if (!controller.signal.aborted) setSubmitting(false);
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
          <p className={styles.baseNotice}>
            Editando a versão principal por{' '}
            <strong>{guide.author || 'Autor do guia'}</strong>. A versão usada
            como base fica preservada, mesmo que outra vença durante sua edição.
          </p>
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
            key={guide.activeRevisionId}
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
              {submitting ? 'Publicando...' : 'Publicar revisão'}
            </button>
          </footer>
        </form>
      )}
    </main>
  );
}

export default CreateRevisionPage;
