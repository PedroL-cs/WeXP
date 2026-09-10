import { CheckIcon } from '@phosphor-icons/react';
import { useEffect, useRef, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router';
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
        const item = await getAchievementGuide(achievementId);

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
      <header>
        <p>Conquista {achievementId}</p>
        <h1>Sugerir revisão</h1>
        <span>Edite o guia e descreva brevemente o que foi alterado.</span>
      </header>
      {loading && <div className={styles.status}>Carregando guia...</div>}
      {!loading && !guide && <div className={styles.status}>{error}</div>}
      {!loading && guide && (
        <form onSubmit={submit}>
          <label htmlFor='summary'>Resumo da alteração</label>
          <input
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
          <footer>
            {error && <span className={styles.error}>{error}</span>}
            <button type='submit' disabled={submitting}>
              <CheckIcon size={20} />
              {submitting ? 'Enviando...' : 'Enviar revisão'}
            </button>
          </footer>
        </form>
      )}
    </main>
  );
}

export default CreateRevisionPage;
