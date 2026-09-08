import { ArrowLeftIcon, CheckIcon } from '@phosphor-icons/react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { createGuideRevision, getAchievementGuide } from '../../api/games';
import GuideEditor from '../../components/GuideEditor';
import type { Guide } from '../../types/Guide';
import styles from './styles.module.css';

function CreateRevisionPage() {
  const navigate = useNavigate();
  const { achievementId } = useParams();
  const [guide, setGuide] = useState<Guide | null>(null);
  const [summary, setSummary] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const markdownRef = useRef('');

  useEffect(() => {
    async function loadGuide() {
      if (!achievementId) { setError('Conquista não encontrada.'); setIsLoading(false); return; }
      try {
        const response = await getAchievementGuide(achievementId);
        markdownRef.current = response.content;
        setGuide(response);
      } catch (requestError) {
        console.error('Erro ao carregar guia:', requestError);
        setError('Não foi possível carregar o guia para revisão.');
      } finally { setIsLoading(false); }
    }
    loadGuide();
  }, [achievementId]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!guide?.id || !markdownRef.current.trim() || !summary.trim()) {
      setError('Informe o conteúdo revisado e um resumo da alteração.');
      return;
    }
    try {
      setError(''); setIsSubmitting(true);
      await createGuideRevision(guide.id, markdownRef.current, summary.trim());
      navigate(`/achievements/${achievementId}/guide?tab=revisions`);
    } catch (requestError) {
      console.error('Erro ao enviar revisão:', requestError);
      setError('Não foi possível enviar a revisão. Tente novamente.');
    } finally { setIsSubmitting(false); }
  }

  return <main className={styles.page}>
    <button type='button' className={styles.backButton} onClick={() => navigate(-1)}><ArrowLeftIcon size={20} />Voltar</button>
    <header className={styles.header}><p className={styles.eyebrow}>Conquista {achievementId}</p><h1>Sugerir revisão</h1><p>Edite o guia e descreva brevemente o que foi alterado. A sugestão será enviada para análise.</p></header>
    {isLoading && <p className={styles.status}>Carregando guia...</p>}
    {!isLoading && error && !guide && <p className={`${styles.status} ${styles.error}`}>{error}</p>}
    {!isLoading && guide && <form onSubmit={handleSubmit} className={styles.form}>
      <label className={styles.label} htmlFor='change-summary'>Resumo da alteração</label>
      <input id='change-summary' className={styles.summary} value={summary} onChange={event => setSummary(event.target.value)} maxLength={255} placeholder='Ex.: Corrige a ordem dos passos finais' required />
      <GuideEditor initialMarkdown={guide.content} onChange={markdown => { markdownRef.current = markdown; }} />
      <div className={styles.footer}><div aria-live='polite'>{error && <p className={styles.error}>{error}</p>}</div><button type='submit' className={styles.submitButton} disabled={isSubmitting}><CheckIcon size={20} />{isSubmitting ? 'Enviando...' : 'Enviar revisão'}</button></div>
    </form>}
  </main>;
}

export default CreateRevisionPage;
