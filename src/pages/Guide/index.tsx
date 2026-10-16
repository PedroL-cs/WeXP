import {
  ArrowFatUpIcon,
  ArrowLeftIcon,
  ArrowRightIcon,
  BookOpenTextIcon,
  ClockCounterClockwiseIcon,
  CrownIcon,
  GitDiffIcon,
  NotePencilIcon,
  PlusIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import { useEffect, type KeyboardEvent, type ReactNode } from 'react';
import { Link, useLocation, useParams, useSearchParams } from 'react-router';
import GuideContent from '../../components/GuideContent';
import GuideDiff from '../../components/GuideDiff';
import { useAuth } from '../../contexts/AuthContext';
import type { GuideRevision } from '../../types/Guide';
import { useGuideData, type AuthorInfo } from './useGuideData';
import styles from './styles.module.css';
import SaveButton from '../../components/SaveButton';

const dateFormat = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'medium',
  timeStyle: 'short',
});
const numberFormat = new Intl.NumberFormat('pt-BR');

function dateLabel(value?: string) {
  if (!value) return '';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : dateFormat.format(date);
}

function tabKey<T extends string>(
  event: KeyboardEvent<HTMLButtonElement>,
  values: readonly T[],
  current: T,
  change: (value: T) => void,
) {
  const index = values.indexOf(current);
  const next =
    event.key === 'ArrowRight'
      ? (index + 1) % values.length
      : event.key === 'ArrowLeft'
        ? (index + values.length - 1) % values.length
        : event.key === 'Home'
          ? 0
          : event.key === 'End'
            ? values.length - 1
            : -1;
  if (next < 0) return;
  event.preventDefault();
  const buttons =
    event.currentTarget.parentElement?.querySelectorAll<HTMLButtonElement>(
      '[role="tab"]',
    );
  change(values[next]);
  buttons?.[next]?.focus();
}

function Notice({
  children,
  retry,
  error = true,
}: {
  children: ReactNode;
  retry?: () => void;
  error?: boolean;
}) {
  return (
    <div
      className={`${styles.notice} ${error ? styles.errorNotice : styles.infoNotice}`}
      role={error ? 'alert' : 'status'}
    >
      <span>{children}</span>
      {retry && (
        <button type='button' onClick={retry}>
          Tentar novamente
        </button>
      )}
    </div>
  );
}

function RevisionBadges({ revision }: { revision: GuideRevision }) {
  return (
    <div className={styles.badges}>
      {revision.isMain && (
        <span className={`${styles.badge} ${styles.mainBadge}`}>
          <CrownIcon size={12} weight='fill' />
          Principal
        </span>
      )}
      {revision.isOriginal && <span className={styles.badge}>Original</span>}
      {!revision.isMain && !revision.isOriginal && (
        <span className={styles.badge}>Publicada</span>
      )}
    </div>
  );
}

function Author({
  id,
  info,
  fallback,
}: {
  id?: string;
  info?: AuthorInfo;
  fallback: string;
}) {
  const name = info?.username || fallback;
  return (
    <span className={styles.authorIdentity}>
      {info?.avatar && (
        <img
          className={styles.revisionAvatar}
          src={info.avatar}
          alt=''
          loading='lazy'
          onError={event => {
            event.currentTarget.style.display = 'none';
          }}
        />
      )}
      {id ? (
        <Link to={`/users/${encodeURIComponent(id)}`}>{name}</Link>
      ) : (
        <span>{name}</span>
      )}
    </span>
  );
}

function GuidePageContent() {
  const { gameId, achievementId } = useParams();
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const [params, setParams] = useSearchParams();
  const tab = params.get('tab') === 'revisions' ? 'revisions' : 'guide';
  const pageParam = params.get('page') ?? '0';
  const page =
    /^\d+$/.test(pageParam) && Number(pageParam) <= 2147483647
      ? Number(pageParam)
      : 0;
  const requestedId = params.get('revision') || null;
  const data = useGuideData(
    achievementId,
    isAuthenticated,
    tab,
    page,
    requestedId,
    params.get('view') === 'changes',
  );
  const { guide, selected, base, revisionPage } = data;
  const view =
    params.get('view') === 'changes' && selected?.baseRevisionId
      ? 'changes'
      : 'content';
  const returnTo = `${location.pathname}${location.search}`;
  const editorPath = `/games/${gameId}/achievements/${achievementId}/guide`;
  const revisionItems =
    revisionPage?.content.map(item => data.versions[item.publicId] ?? item) ??
    [];

  useEffect(() => {
    if (
      data.historyLoading ||
      !revisionPage ||
      !revisionPage.totalPages ||
      page < revisionPage.totalPages
    )
      return;
    setParams(
      previous => {
        const next = new URLSearchParams(previous);
        next.set('page', String(revisionPage.totalPages - 1));
        return next;
      },
      { replace: true },
    );
  }, [data.historyLoading, revisionPage, page, setParams]);

  function changeTab(nextTab: 'guide' | 'revisions') {
    setParams(previous => {
      const next = new URLSearchParams(previous);
      if (nextTab === 'revisions') next.set('tab', 'revisions');
      else next.delete('tab');
      return next;
    });
  }

  function changeView(nextView: 'content' | 'changes') {
    setParams(
      previous => {
        const next = new URLSearchParams(previous);
        if (nextView === 'changes') next.set('view', 'changes');
        else next.delete('view');
        return next;
      },
      { replace: true },
    );
  }

  function chooseRevision(id: string, scroll = false) {
    setParams(previous => {
      const next = new URLSearchParams(previous);
      next.set('tab', 'revisions');
      next.set('revision', id);
      return next;
    });
    if (scroll && window.matchMedia('(max-width: 960px)').matches) {
      requestAnimationFrame(() => {
        const panel = document.getElementById('revision-panel');
        panel?.focus({ preventScroll: true });
        panel?.scrollIntoView({
          behavior: window.matchMedia('(prefers-reduced-motion: reduce)')
            .matches
            ? 'auto'
            : 'smooth',
          block: 'start',
        });
      });
    }
  }

  function changePage(nextPage: number) {
    setParams(previous => {
      const next = new URLSearchParams(previous);
      next.set('page', String(nextPage));
      next.delete('revision');
      return next;
    });
  }

  function vote(revision: GuideRevision) {
    // Keep the version being read open even if this vote elects another main.
    if (!requestedId && data.selectedId) chooseRevision(data.selectedId);
    void data.toggleVote(revision);
  }

  function voteControl(revision: GuideRevision) {
    if (!isAuthenticated || data.sessionExpired) {
      return (
        <Link
          className={styles.voteLogin}
          to='/login'
          state={{ from: returnTo }}
        >
          <ArrowFatUpIcon size={16} />
          {numberFormat.format(revision.voteCount)} ·{' '}
          {data.sessionExpired
            ? 'Entre novamente para votar'
            : 'Entre para votar'}
        </Link>
      );
    }
    const own = revision.authorPublicId === data.user?.publicId;
    const pending = data.pendingVoteId === revision.publicId;
    const label = own
      ? 'Sua versão'
      : pending
        ? 'Salvando…'
        : data.profileLoading
          ? 'Verificando…'
          : !data.user
            ? 'Perfil indisponível'
            : revision.viewerHasVoted
              ? 'Votado'
              : 'Votar';
    return (
      <button
        type='button'
        className={`${styles.voteButton} ${revision.viewerHasVoted ? styles.voted : ''}`}
        onClick={() => vote(revision)}
        disabled={own || Boolean(data.pendingVoteId) || !data.user}
        aria-pressed={revision.viewerHasVoted}
        aria-busy={pending}
        aria-label={
          own
            ? 'Você não pode votar na própria versão'
            : `${revision.viewerHasVoted ? 'Retirar voto de' : 'Votar em'}: ${revision.changeSummary || 'Versão do guia'}`
        }
        title={
          own
            ? 'Autores não podem votar nas próprias versões.'
            : revision.viewerHasVoted
              ? 'Clique para retirar seu voto'
              : 'Apoiar esta versão'
        }
      >
        <ArrowFatUpIcon
          size={17}
          weight={revision.viewerHasVoted ? 'fill' : 'bold'}
        />
        <strong>{numberFormat.format(revision.voteCount)}</strong>
        <span>{label}</span>
      </button>
    );
  }

  const publishedNotice =
    location.state?.publishedRevisionId === selected?.publicId
      ? 'Revisão publicada! Ela começa com zero votos e já pode ser avaliada pela comunidade.'
      : '';

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        {(data.achievement?.icon_url || data.achievement?.icon) && (
          <div
            className={styles.ambient}
            style={{
              backgroundImage: `url(${data.achievement.icon_url || data.achievement.icon})`,
            }}
            aria-hidden='true'
          />
        )}
        <div className={styles.iconFrame}>
          {data.achievement?.icon_url || data.achievement?.icon ? (
            <img
              className={styles.achievementIcon}
              src={
                data.achievement.icon_url || data.achievement.icon || undefined
              }
              alt={`Ícone da conquista ${data.achievement.name}`}
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
          <h1>
            {data.achievement?.name ||
              (data.guideLoading ? 'Carregando conquista…' : 'Conquista')}
          </h1>
          {data.achievement?.description && (
            <p className={styles.achievementDescription}>
              {data.achievement.description}
            </p>
          )}
          {guide && (
            <div className={styles.guideMetadata}>
              <span className={styles.mainLabel}>
                <CrownIcon size={14} weight='fill' />
                Versão principal
              </span>
              <span>
                {numberFormat.format(guide.voteCount)}{' '}
                {guide.voteCount === 1 ? 'voto' : 'votos'}
              </span>
              {data.guideLoading && (
                <span className={styles.refreshLabel}>Atualizando…</span>
              )}
            </div>
          )}
          {guide && (
            <div className={styles.author}>
              Versão por{' '}
              <Author
                id={guide.authorPublicId}
                info={data.authors[guide.authorPublicId]}
                fallback={guide.author || 'Autor do guia'}
              />
              {guide.creatorPublicId !== guide.authorPublicId && (
                <span className={styles.creator}>
                  Guia criado originalmente por{' '}
                  <Author
                    id={guide.creatorPublicId}
                    info={data.authors[guide.creatorPublicId]}
                    fallback={guide.creator || 'Criador do guia'}
                  />
                </span>
              )}
            </div>
          )}
        </div>
        {guide && (
          <div className={styles.headerActions}>
            <SaveButton type='GUIDE' resourceId={guide.id} />
            {isAuthenticated && (
              <Link className={styles.outline} to={`${editorPath}/revise`}>
                <NotePencilIcon size={18} />
                Sugerir revisão
              </Link>
            )}
          </div>
        )}
      </header>

      {data.achievementError && (
        <Notice retry={data.refresh}>{data.achievementError}</Notice>
      )}
      {data.guideLoading && !guide && (
        <p className={styles.status} role='status'>
          Carregando guia…
        </p>
      )}
      {data.guideError && (
        <Notice retry={data.refresh}>
          {guide
            ? `Não foi possível atualizar o guia. ${data.voteFeedback ? 'Seu voto foi salvo; ' : ''}a última versão carregada continua disponível.`
            : data.guideError}
        </Notice>
      )}

      {!data.guideLoading && !data.guideError && !guide && data.achievement && (
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
            <Link className={styles.primaryButton} to={`${editorPath}/create`}>
              <PlusIcon size={18} weight='bold' />
              Criar primeiro guia
            </Link>
          ) : (
            <Link
              className={styles.primaryButton}
              to='/login'
              state={{ from: `${editorPath}/create` }}
            >
              Entre para escrever o primeiro guia
            </Link>
          )}
        </section>
      )}

      {guide && (
        <>
          <div
            className={styles.tabs}
            role='tablist'
            aria-label='Guia e revisões'
          >
            <button
              type='button'
              role='tab'
              id='guide-tab'
              aria-controls='guide-panel'
              aria-selected={tab === 'guide'}
              tabIndex={tab === 'guide' ? 0 : -1}
              className={tab === 'guide' ? styles.active : ''}
              onClick={() => changeTab('guide')}
              onKeyDown={event =>
                tabKey(event, ['guide', 'revisions'], tab, changeTab)
              }
            >
              <BookOpenTextIcon size={16} />
              Guia principal
            </button>
            <button
              type='button'
              role='tab'
              id='revisions-tab'
              aria-controls='revisions-panel'
              aria-selected={tab === 'revisions'}
              tabIndex={tab === 'revisions' ? 0 : -1}
              className={tab === 'revisions' ? styles.active : ''}
              onClick={() => changeTab('revisions')}
              onKeyDown={event =>
                tabKey(event, ['guide', 'revisions'], tab, changeTab)
              }
            >
              <ClockCounterClockwiseIcon size={16} />
              Revisões
            </button>
          </div>

          {tab === 'guide' && (
            <section
              id='guide-panel'
              role='tabpanel'
              aria-labelledby='guide-tab'
              tabIndex={0}
              aria-busy={data.guideLoading}
            >
              <div className={styles.mainToolbar}>
                <p>A versão principal é escolhida pelos votos da comunidade.</p>
                {guide.activeRevisionId && (
                  <button
                    type='button'
                    onClick={() => chooseRevision(guide.activeRevisionId!)}
                  >
                    <ClockCounterClockwiseIcon size={16} />
                    Ver esta versão nas revisões
                    <ArrowRightIcon size={14} />
                  </button>
                )}
              </div>
              <GuideContent markdown={guide.content} />
            </section>
          )}

          {tab === 'revisions' && (
            <section
              id='revisions-panel'
              className={styles.history}
              role='tabpanel'
              aria-labelledby='revisions-tab'
              tabIndex={0}
            >
              <div className={styles.historyHeader}>
                <div>
                  <h2>Versões da comunidade</h2>
                  <p>
                    Leia, compare as alterações e apoie as melhores versões. Em
                    empate, a principal permanece.
                  </p>
                </div>
                <button
                  type='button'
                  className={styles.refreshButton}
                  onClick={data.refresh}
                  disabled={data.historyLoading || Boolean(data.pendingVoteId)}
                >
                  Atualizar
                </button>
              </div>
              {data.voteFeedback || publishedNotice ? (
                <Notice error={false}>
                  {data.voteFeedback || publishedNotice}
                </Notice>
              ) : null}
              {data.voteError && <Notice>{data.voteError}</Notice>}
              {data.profileError && (
                <Notice
                  retry={data.sessionExpired ? undefined : data.retryProfile}
                >
                  {data.sessionExpired
                    ? 'Sua sessão expirou. Entre novamente para votar.'
                    : `${data.profileError} Você ainda pode ler e comparar as versões.`}
                </Notice>
              )}
              {data.historyError && (
                <Notice retry={data.refresh}>
                  Não foi possível atualizar a lista de revisões.
                  {data.voteFeedback ? ' Seu voto foi salvo.' : ''} Tente
                  novamente.
                </Notice>
              )}
              {data.historyLoading && (
                <p className={styles.loadingLine} role='status'>
                  Atualizando revisões…
                </p>
              )}
              {!data.historyLoading &&
                !data.historyError &&
                revisionPage?.totalElements === 1 && (
                  <p className={styles.singleVersion}>
                    Por enquanto, temos apenas a versão original. Você pode
                    publicar uma revisão para contribuir.
                  </p>
                )}

              <div className={styles.grid}>
                <aside
                  className={styles.list}
                  aria-label='Lista de versões'
                  aria-busy={data.historyLoading}
                >
                  <div className={styles.listHeading}>
                    <strong>
                      {data.pagination
                        ? `${numberFormat.format(data.pagination.totalElements)} versões`
                        : 'Versões'}
                    </strong>
                    <span>Mais votadas primeiro</span>
                  </div>
                  <div className={styles.cards}>
                    {revisionItems.map(revision => (
                      <article
                        key={revision.publicId}
                        className={`${styles.revisionCard} ${selected?.publicId === revision.publicId ? styles.selected : ''}`}
                      >
                        <button
                          type='button'
                          className={styles.revisionSelect}
                          onClick={() =>
                            chooseRevision(revision.publicId, true)
                          }
                          aria-controls='revision-panel'
                          aria-current={
                            selected?.publicId === revision.publicId
                              ? 'true'
                              : undefined
                          }
                        >
                          <strong>
                            {revision.isOriginal
                              ? 'Versão original do guia'
                              : revision.changeSummary || 'Revisão publicada'}
                          </strong>
                          <RevisionBadges revision={revision} />
                          {revision.createdAt && (
                            <time
                              dateTime={revision.createdAt}
                              className={styles.revisionDate}
                            >
                              {dateLabel(revision.createdAt)}
                            </time>
                          )}
                        </button>
                        <div className={styles.authorRow}>
                          Por{' '}
                          <Author
                            id={revision.authorPublicId}
                            info={data.authors[revision.authorPublicId]}
                            fallback={revision.author || 'Autor da versão'}
                          />
                        </div>
                        <div className={styles.cardActions}>
                          {voteControl(revision)}
                        </div>
                      </article>
                    ))}
                    {!data.historyLoading &&
                      !data.historyError &&
                      revisionItems.length === 0 && (
                        <p className={styles.listEmpty}>
                          Nenhuma versão nesta página.
                        </p>
                      )}
                  </div>
                  {data.pagination && data.pagination.totalPages > 1 && (
                    <nav
                      className={styles.pagination}
                      aria-label='Páginas de revisões'
                    >
                      <button
                        type='button'
                        onClick={() => changePage(page - 1)}
                        disabled={page === 0 || data.historyLoading}
                        aria-label='Página anterior'
                      >
                        <ArrowLeftIcon size={17} />
                      </button>
                      <span>
                        Página {page + 1} de {data.pagination.totalPages}
                      </span>
                      <button
                        type='button'
                        onClick={() => changePage(page + 1)}
                        disabled={
                          page + 1 >= data.pagination.totalPages ||
                          data.historyLoading
                        }
                        aria-label='Próxima página'
                      >
                        <ArrowRightIcon size={17} />
                      </button>
                    </nav>
                  )}
                </aside>

                <section
                  id='revision-panel'
                  className={styles.diffPanel}
                  tabIndex={-1}
                  aria-label='Detalhes da versão selecionada'
                >
                  {data.selectionError && (
                    <Notice retry={data.refresh}>{data.selectionError}</Notice>
                  )}
                  {!selected && (
                    <p className={styles.status} role='status'>
                      {data.selectionLoading || data.historyLoading
                        ? 'Carregando versão…'
                        : 'Selecione uma versão para ler e comparar.'}
                    </p>
                  )}
                  {selected && (
                    <>
                      <header className={styles.versionHeader}>
                        <div>
                          <RevisionBadges revision={selected} />
                          <h2 aria-live='polite'>
                            {selected.isOriginal
                              ? 'Versão original do guia'
                              : selected.changeSummary || 'Revisão publicada'}
                          </h2>
                          <p className={styles.versionByline}>
                            Por{' '}
                            <Author
                              id={selected.authorPublicId}
                              info={data.authors[selected.authorPublicId]}
                              fallback={selected.author || 'Autor da versão'}
                            />
                            {selected.createdAt && (
                              <time dateTime={selected.createdAt}>
                                {dateLabel(selected.createdAt)}
                              </time>
                            )}
                          </p>
                        </div>
                        {voteControl(selected)}
                      </header>
                      {!revisionItems.some(
                        item => item.publicId === selected.publicId,
                      ) && (
                        <p className={styles.contextNote}>
                          A versão selecionada não está na página atual da
                          lista. Ela continua aberta para você.
                        </p>
                      )}
                      {!selected.isMain &&
                        !selected.isOriginal &&
                        !selected.isBasedOnCurrentMain && (
                          <p className={styles.contextNote}>
                            Esta revisão usa uma base que não é mais a
                            principal. Ela continua disponível para avaliação e
                            votação.
                          </p>
                        )}
                      <div
                        className={`${styles.tabs} ${styles.detailTabs}`}
                        role='tablist'
                        aria-label='Visualização da versão'
                      >
                        <button
                          type='button'
                          role='tab'
                          id='content-tab'
                          aria-controls='version-content'
                          aria-selected={view === 'content'}
                          tabIndex={view === 'content' ? 0 : -1}
                          className={view === 'content' ? styles.active : ''}
                          onClick={() => changeView('content')}
                          onKeyDown={event =>
                            tabKey(
                              event,
                              selected.baseRevisionId
                                ? ['content', 'changes']
                                : ['content'],
                              view,
                              changeView,
                            )
                          }
                        >
                          <BookOpenTextIcon size={16} />
                          Conteúdo
                        </button>
                        <button
                          type='button'
                          role='tab'
                          id='changes-tab'
                          aria-controls='version-changes'
                          aria-selected={view === 'changes'}
                          tabIndex={view === 'changes' ? 0 : -1}
                          className={view === 'changes' ? styles.active : ''}
                          onClick={() => changeView('changes')}
                          disabled={!selected.baseRevisionId}
                          title={
                            !selected.baseRevisionId
                              ? 'A versão original não possui uma versão anterior para comparar.'
                              : 'Comparar com a versão usada como base'
                          }
                          onKeyDown={event =>
                            tabKey(
                              event,
                              ['content', 'changes'],
                              view,
                              changeView,
                            )
                          }
                        >
                          <GitDiffIcon size={17} />
                          Alterações
                        </button>
                      </div>
                      {view === 'content' && (
                        <div
                          id='version-content'
                          role='tabpanel'
                          aria-labelledby='content-tab'
                          tabIndex={0}
                        >
                          {selected.isOriginal && (
                            <p className={styles.contextNote}>
                              Esta é a primeira versão. Selecione uma revisão e
                              abra “Alterações” para ver o diff.
                            </p>
                          )}
                          <GuideContent markdown={selected.content} />
                        </div>
                      )}
                      {view === 'changes' && (
                        <div
                          id='version-changes'
                          role='tabpanel'
                          aria-labelledby='changes-tab'
                          tabIndex={0}
                        >
                          <p className={styles.diffDescription}>
                            Comparação com a versão realmente usada nesta
                            edição, mesmo que a principal tenha mudado.
                          </p>
                          <p className={styles.note}>
                            <span className={styles.legendRemoved}>
                              Removido
                            </span>
                            <span className={styles.legendChanged}>
                              Alterado
                            </span>
                            <span className={styles.legendAdded}>
                              Adicionado
                            </span>
                          </p>
                          {data.baseLoading && !base && (
                            <p className={styles.status} role='status'>
                              Carregando a versão usada como base…
                            </p>
                          )}
                          {data.baseError && (
                            <Notice retry={data.retryBase}>
                              {data.baseError}
                            </Notice>
                          )}
                          {base && (
                            <>
                              {base.content === selected.content && (
                                <p className={styles.contextNote}>
                                  O conteúdo desta revisão é igual ao da versão
                                  usada como base.
                                </p>
                              )}
                              <GuideDiff
                                previous={base.content}
                                current={selected.content}
                                previousAuthor={
                                  data.authors[base.authorPublicId] ?? {
                                    username: base.author || 'Autor da base',
                                  }
                                }
                                currentAuthor={
                                  data.authors[selected.authorPublicId] ?? {
                                    username:
                                      selected.author || 'Autor da revisão',
                                  }
                                }
                              />
                            </>
                          )}
                        </div>
                      )}
                    </>
                  )}
                </section>
              </div>
            </section>
          )}
        </>
      )}
    </main>
  );
}

function GuidePage() {
  const { achievementId } = useParams();
  return <GuidePageContent key={achievementId ?? 'missing'} />;
}

export default GuidePage;
