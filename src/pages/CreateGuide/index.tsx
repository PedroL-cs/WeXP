import { useEffect, useRef, useState } from 'react';
import { ArrowLeftIcon, CheckIcon } from '@phosphor-icons/react';
import { Navigate, useNavigate, useParams } from 'react-router';

import { createAchievementGuide } from '../../api/guides';
import GuideEditor from '../../components/GuideEditor';
import styles from './styles.module.css';
import { getAchievement } from '../../api/achievements';
import type { Achievement } from '../../types/Achievement';
import { useAuth } from '../../contexts/AuthContext';

const initialMarkdown = `# Como desbloquear

Escreva aqui o passo a passo para desbloquear esta conquista.
`;

function CreateGuidePage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { achievementId } = useParams();

  const markdownRef = useRef(initialMarkdown);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const [achievement, setAchievement] = useState<Achievement | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadAchievement() {
      if (!achievementId) {
        setError('Conquista não encontrada.');
        setLoading(false);
        return;
      }

      try {
        const data = await getAchievement(achievementId);
        setAchievement(data);
      } catch (error) {
        console.error('Erro ao carregar conquista:', error);
        setError('Não foi possível carregar a conquista.');
      } finally {
        setLoading(false);
      }
    }

    loadAchievement();
  }, [achievementId]);

  if (!isAuthenticated) {
    return <Navigate to='/login' replace />;
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!achievementId || !markdownRef.current.trim()) {
      setError('Escreva o conteúdo do guia antes de publicar.');
      return;
    }

    try {
      setError('');
      setSuccess(false);
      setIsSubmitting(true);

      await createAchievementGuide(achievementId, markdownRef.current);
      setSuccess(true);
      navigate(`/achievements/${achievementId}/guide`);
    } catch (requestError) {
      console.error('Erro ao criar guia:', requestError);
      setError(
        requestError instanceof Error &&
          requestError.message === 'Usuário não autenticado.'
          ? 'Sua sessão expirou. Faça login novamente.'
          : 'Não foi possível criar o guia. Tente novamente.',
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className={styles.page}>
      <div className={styles.header}>
        <button
          type='button'
          className={styles.backButton}
          onClick={() => navigate(-1)}
        >
          <ArrowLeftIcon size={20} />
          Voltar
        </button>

        <div>
          <p className={styles.eyebrow}>Criar guia</p>

          <h1>{achievement?.name ?? 'Conquista'}</h1>

          <p className={styles.description}>
            Compartilhe um passo a passo claro para ajudar outros jogadores a
            desbloquear esta conquista.
          </p>
        </div>
      </div>

      <form className={styles.form} onSubmit={handleSubmit}>
        <GuideEditor
          initialMarkdown={initialMarkdown}
          onChange={markdown => {
            markdownRef.current = markdown;
          }}
        />

        <div className={styles.footer}>
          <div aria-live='polite'>
            {error && <p className={styles.error}>{error}</p>}
            {success && (
              <p className={styles.success}>Guia criado com sucesso.</p>
            )}
          </div>

          <button
            type='submit'
            className={styles.submitButton}
            disabled={isSubmitting}
          >
            <CheckIcon size={20} />
            {isSubmitting ? 'Publicando...' : 'Publicar guia'}
          </button>
        </div>
      </form>
    </main>
  );
}

export default CreateGuidePage;
