import { ArrowLeftIcon, ClockCounterClockwiseIcon, NotePencilIcon } from '@phosphor-icons/react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router';
import { getAchievementGuide, getGuideRevisions } from '../../api/games';
import GuideContent from '../../components/GuideContent';
import GuideDiff from '../../components/GuideDiff';
import type { Guide, GuideRevision } from '../../types/Guide';
import styles from './styles.module.css';

function formatDate(date?: string) {
  return date ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(date)) : 'Data não informada';
}

function GuidePage() {
  const navigate = useNavigate();
  const { achievementId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const [guide, setGuide] = useState<Guide | null>(null);
  const [revisions, setRevisions] = useState<GuideRevision[]>([]);
  const [selectedRevision, setSelectedRevision] = useState<GuideRevision | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingRevisions, setIsLoadingRevisions] = useState(false);
  const [error, setError] = useState('');
  const [revisionsError, setRevisionsError] = useState('');
  const activeTab = searchParams.get('tab') === 'revisions' ? 'revisions' : 'guide';

  useEffect(() => {
    async function loadGuide() {
      if (!achievementId) { setError('Conquista não encontrada.'); setIsLoading(false); return; }
      try { setError(''); setIsLoading(true); setGuide(await getAchievementGuide(achievementId)); }
      catch (requestError) { console.error('Erro ao carregar guia:', requestError); setError('Não foi possível carregar o guia.'); }
      finally { setIsLoading(false); }
    }
    loadGuide();
  }, [achievementId]);

  useEffect(() => {
    async function loadRevisions() {
      if (activeTab !== 'revisions' || !guide?.id) return;
      try {
        setIsLoadingRevisions(true); setRevisionsError('');
        const items = await getGuideRevisions(guide.id);
        setRevisions(items); setSelectedRevision(current => current ?? items[0] ?? null);
      } catch (requestError) {
        console.error('Erro ao carregar revisões:', requestError);
        setRevisionsError('Não foi possível carregar o histórico de revisões.');
      } finally { setIsLoadingRevisions(false); }
    }
    loadRevisions();
  }, [activeTab, guide?.id]);

  function selectTab(tab: 'guide' | 'revisions') { setSearchParams(tab === 'revisions' ? { tab } : {}); }

  return <main className={styles.page}>
    <button type='button' className={styles.backButton} onClick={() => navigate(-1)}><ArrowLeftIcon size={20} />Voltar</button>
    <header className={styles.header}>
      <div><p className={styles.eyebrow}>Conquista {achievementId}</p><h1>Guia da conquista</h1>{guide?.authorUsername && <p className={styles.metadata}>Por {guide.authorUsername}</p>}</div>
      {guide?.id && <button type='button' className={styles.reviseButton} onClick={() => navigate(`/achievements/${achievementId}/guide/revise`)}><NotePencilIcon size={20} />Sugerir revisão</button>}
    </header>
    {isLoading && <p className={styles.status}>Carregando guia...</p>}
    {!isLoading && error && <p className={`${styles.status} ${styles.error}`}>{error}</p>}
    {!isLoading && !error && !guide?.content && <p className={styles.status}>Esta conquista ainda não possui um guia.</p>}
    {!isLoading && !error && guide?.content && <>
      <div className={styles.tabs} role='tablist' aria-label='Conteúdo do guia'>
        <button type='button' role='tab' aria-selected={activeTab === 'guide'} className={activeTab === 'guide' ? styles.activeTab : styles.tab} onClick={() => selectTab('guide')}>Guia atual</button>
        <button type='button' role='tab' aria-selected={activeTab === 'revisions'} className={activeTab === 'revisions' ? styles.activeTab : styles.tab} onClick={() => selectTab('revisions')}><ClockCounterClockwiseIcon size={18} />Revisões</button>
      </div>
      {activeTab === 'guide' && <GuideContent markdown={guide.content} />}
      {activeTab === 'revisions' && <section className={styles.revisions} aria-label='Histórico de revisões'>
        {isLoadingRevisions && <p className={styles.status}>Carregando revisões...</p>}
        {revisionsError && <p className={`${styles.status} ${styles.error}`}>{revisionsError}</p>}
        {!isLoadingRevisions && !revisionsError && revisions.length === 0 && <p className={styles.status}>Ainda não há revisões sugeridas.</p>}
        {!isLoadingRevisions && !revisionsError && revisions.length > 0 && <div className={styles.revisionLayout}>
          <div className={styles.revisionList}>{revisions.map(revision => <button type='button' key={revision.publicId} className={selectedRevision?.publicId === revision.publicId ? styles.selectedRevision : styles.revision} onClick={() => setSelectedRevision(revision)}><strong>{revision.changeSummary || 'Alteração sugerida'}</strong><span>{formatDate(revision.createdAt)}</span><small>{revision.status === 'PENDING' ? 'Pendente' : revision.status ?? 'Sem status'}</small></button>)}</div>
          {selectedRevision && <div className={styles.comparison}><div className={styles.comparisonHeader}><div><h2>Diferenças propostas</h2><p>Vermelho indica remoção; verde indica adição.</p></div><span>{selectedRevision.changeSummary || 'Alteração sugerida'}</span></div><GuideDiff previous={guide.content} current={selectedRevision.content} /></div>}
        </div>}
      </section>}
    </>}
  </main>;
}

export default GuidePage;
