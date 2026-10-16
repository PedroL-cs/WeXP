import {
  ArrowLeftIcon,
  ArrowRightIcon,
  BookBookmarkIcon,
  BookOpenTextIcon,
  CaretRightIcon,
  ClipboardTextIcon,
  GameControllerIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { getAchievement } from '../../api/achievements';
import { getGame } from '../../api/games';
import { getGuide } from '../../api/guides';
import { getSavedItems } from '../../api/savedItems';
import SaveButton from '../../components/SaveButton';
import type { AchievementDetails } from '../../types/Achievement';
import type { Game } from '../../types/Game';
import type { Guide } from '../../types/Guide';
import type { SavedItem, SavedResourceType } from '../../types/SavedItem';
import styles from './styles.module.css';

type LibraryPageProps = { type?: SavedResourceType };

type ResolvedItem =
  | { saved: SavedItem; type: 'GAME'; game: Game }
  | {
      saved: SavedItem;
      type: 'ACHIEVEMENT';
      achievement: AchievementDetails;
    }
  | {
      saved: SavedItem;
      type: 'GUIDE';
      guide: Guide;
      achievement: AchievementDetails;
    };

const pageInfo = {
  ALL: {
    eyebrow: 'Sua coleção',
    title: 'Biblioteca',
    description: 'Tudo que você separou para jogar, conquistar ou consultar depois.',
    icon: BookBookmarkIcon,
  },
  GAME: {
    eyebrow: 'Biblioteca',
    title: 'Jogos salvos',
    description: 'Os mundos que você quer manter por perto.',
    icon: GameControllerIcon,
  },
  ACHIEVEMENT: {
    eyebrow: 'Biblioteca',
    title: 'Conquistas salvas',
    description: 'Seus próximos objetivos, reunidos em um só lugar.',
    icon: TrophyIcon,
  },
  GUIDE: {
    eyebrow: 'Biblioteca',
    title: 'Guias salvos',
    description: 'Passos e estratégias para consultar quando precisar.',
    icon: BookOpenTextIcon,
  },
} as const;

const typeLabels: Record<SavedResourceType, string> = {
  GAME: 'Jogo',
  ACHIEVEMENT: 'Conquista',
  GUIDE: 'Guia',
};

const resourceTypes: SavedResourceType[] = ['GAME', 'ACHIEVEMENT', 'GUIDE'];

const groupInfo: Record<
  SavedResourceType,
  { title: string; description: string; href: string }
> = {
  GAME: {
    title: 'Jogos',
    description: 'Mundos para explorar',
    href: '/library/games',
  },
  ACHIEVEMENT: {
    title: 'Conquistas',
    description: 'Objetivos para desbloquear',
    href: '/library/achievements',
  },
  GUIDE: {
    title: 'Guias',
    description: 'Estratégias para consultar',
    href: '/library/guides',
  },
};

async function resolveItem(
  item: SavedItem,
  signal: AbortSignal,
): Promise<ResolvedItem> {
  if (item.type === 'GAME') {
    return { saved: item, type: 'GAME', game: await getGame(item.resourceId) };
  }

  if (item.type === 'ACHIEVEMENT') {
    return {
      saved: item,
      type: 'ACHIEVEMENT',
      achievement: await getAchievement(item.resourceId, signal),
    };
  }

  const guide = await getGuide(item.resourceId, signal);
  if (!guide.achievementId) throw new Error('Guia sem conquista associada.');
  return {
    saved: item,
    type: 'GUIDE',
    guide,
    achievement: await getAchievement(guide.achievementId, signal),
  };
}

function cardInfo(item: ResolvedItem) {
  if (item.type === 'GAME') {
    return {
      href: `/games/${item.game.id}`,
      image: item.game.images.header,
      title: item.game.name,
      subtitle: item.game.shortDescription || 'Jogo salvo na sua biblioteca.',
      context: `${item.game.achievements.length} conquistas`,
    };
  }

  const achievement = item.achievement;
  const href = `/games/${achievement.game.id}/achievements/${achievement.id}/guide`;
  if (item.type === 'ACHIEVEMENT') {
    return {
      href,
      image: achievement.icon,
      title: achievement.name,
      subtitle: achievement.description || 'Conquista salva na sua biblioteca.',
      context: achievement.game.name,
    };
  }

  return {
    href,
    image: achievement.icon,
    title: `Guia: ${achievement.name}`,
    subtitle: item.guide.content
      .replace(/[#_*`>]/g, '')
      .replaceAll('[', '')
      .replaceAll(']', '')
      .replace(/\s+/g, ' ')
      .trim()
      .slice(0, 180),
    context: achievement.game.name,
  };
}

export default function LibraryPage({ type }: LibraryPageProps) {
  const [searchParams, setSearchParams] = useSearchParams();
  const requestedPage = Number(searchParams.get('page') ?? '0');
  const page = Number.isInteger(requestedPage) && requestedPage >= 0 ? requestedPage : 0;
  const [items, setItems] = useState<ResolvedItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [groupTotals, setGroupTotals] = useState<
    Record<SavedResourceType, number>
  >({ GAME: 0, ACHIEVEMENT: 0, GUIDE: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const info = pageInfo[type ?? 'ALL'];
  const PageIcon = info.icon;

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    setItems([]);

    async function load() {
      if (!type) {
        const results = await Promise.allSettled(
          resourceTypes.map(resourceType =>
            getSavedItems(resourceType, 0, 6, controller.signal),
          ),
        );
        if (controller.signal.aborted) return;

        const pages = results.flatMap((result, index) =>
          result.status === 'fulfilled'
            ? [{ type: resourceTypes[index], page: result.value }]
            : [],
        );
        const resolved = await Promise.allSettled(
          pages
            .flatMap(result => result.page.content)
            .map(item => resolveItem(item, controller.signal)),
        );
        if (controller.signal.aborted) return;

        const totals = { GAME: 0, ACHIEVEMENT: 0, GUIDE: 0 };
        pages.forEach(result => {
          totals[result.type] = result.page.totalElements;
        });
        setGroupTotals(totals);
        setTotalElements(
          Object.values(totals).reduce((sum, value) => sum + value, 0),
        );
        setTotalPages(0);
        setItems(
          resolved.flatMap(item =>
            item.status === 'fulfilled' ? [item.value] : [],
          ),
        );
        if (
          results.some(result => result.status === 'rejected') ||
          resolved.some(result => result.status === 'rejected')
        ) {
          setError('Alguns itens não puderam ser carregados.');
        }
        return;
      }

      const result = await getSavedItems(type, page, 20, controller.signal);
      const resolved = await Promise.allSettled(
        result.content.map(item => resolveItem(item, controller.signal)),
      );
      if (controller.signal.aborted) return;
      setItems(
        resolved.flatMap(item =>
          item.status === 'fulfilled' ? [item.value] : [],
        ),
      );
      setTotalElements(result.totalElements);
      setTotalPages(result.totalPages);
      if (resolved.some(item => item.status === 'rejected')) {
        setError('Alguns itens não puderam ser carregados.');
      }
    }

    load()
      .catch(requestError => {
        if (controller.signal.aborted) return;
        setItems([]);
        setError(
          requestError instanceof Error
            ? requestError.message
            : 'Não foi possível carregar sua biblioteca.',
        );
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [page, type]);

  function changePage(nextPage: number) {
    setSearchParams(nextPage > 0 ? { page: String(nextPage) } : {});
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function removeFromList(
    resourceType: SavedResourceType,
    resourceId: string,
  ) {
    setItems(current =>
      current.filter(
        item => item.type !== resourceType || item.saved.resourceId !== resourceId,
      ),
    );
    setTotalElements(current => Math.max(0, current - 1));
    setGroupTotals(current => ({
      ...current,
      [resourceType]: Math.max(0, current[resourceType] - 1),
    }));
  }

  function itemCard(item: ResolvedItem, index: number) {
    const card = cardInfo(item);
    const FallbackIcon =
      item.type === 'GAME'
        ? GameControllerIcon
        : item.type === 'GUIDE'
          ? ClipboardTextIcon
          : TrophyIcon;
    const frameClass =
      item.type === 'GAME'
        ? styles.gameFrame
        : item.type === 'GUIDE'
          ? styles.guideFrame
          : styles.achievementFrame;

    return (
      <article
        className={styles.card}
        key={`${item.type}-${item.saved.resourceId}`}
        style={{ animationDelay: `${Math.min(index, 10) * 35}ms` }}
      >
        <Link className={styles.cardLink} to={card.href}>
          <div className={`${styles.imageFrame} ${frameClass}`}>
            {card.image ? (
              <img
                src={card.image}
                alt=''
                loading='lazy'
                onError={event => (event.currentTarget.hidden = true)}
              />
            ) : (
              <FallbackIcon size={28} weight='fill' />
            )}
            {item.type === 'GUIDE' && (
              <span className={styles.guideOverlay} aria-hidden='true'>
                <ClipboardTextIcon size={17} weight='fill' />
              </span>
            )}
          </div>
          <div className={styles.cardContent}>
            <div className={styles.meta}>
              <span>{typeLabels[item.type]}</span>
              <span>{card.context}</span>
            </div>
            <h2>{card.title}</h2>
            <p>{card.subtitle}</p>
          </div>
        </Link>
        <SaveButton
          type={item.type}
          resourceId={item.saved.resourceId}
          initialSaved
          compact
          onSavedChange={saved => {
            if (!saved) removeFromList(item.type, item.saved.resourceId);
          }}
        />
      </article>
    );
  }

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerIcon}>
          <PageIcon size={30} weight='duotone' />
        </div>
        <div>
          <p className={styles.eyebrow}>{info.eyebrow}</p>
          <h1>{info.title}</h1>
          <p className={styles.description}>{info.description}</p>
        </div>
        {!loading && totalElements > 0 && (
          <span className={styles.count}>{totalElements}</span>
        )}
      </header>

      {error && (
        <p className={styles.notice} role='status'>
          {error}
        </p>
      )}

      {loading ? (
        <div className={styles.list} aria-busy='true'>
          {Array.from({ length: 4 }, (_, index) => (
            <div className={styles.skeleton} key={index} />
          ))}
        </div>
      ) : items.length === 0 ? (
        <section className={styles.empty}>
          <PageIcon size={42} weight='duotone' />
          <h2>Nada salvo por aqui</h2>
          <p>Use o botão Salvar em jogos, conquistas e guias para montar sua coleção.</p>
          <Link to='/'>Explorar jogos</Link>
        </section>
      ) : !type ? (
        <div className={styles.groups}>
          {resourceTypes.map(resourceType => {
            const group = groupInfo[resourceType];
            const groupItems = items.filter(item => item.type === resourceType);
            return (
              <section className={styles.group} key={resourceType}>
                <header className={styles.groupHeader}>
                  <div>
                    <h2>{group.title}</h2>
                    <p>{group.description}</p>
                  </div>
                  <Link to={group.href}>
                    Ver todos
                    {groupTotals[resourceType] > 0 && (
                      <span>{groupTotals[resourceType]}</span>
                    )}
                    <CaretRightIcon size={16} weight='bold' />
                  </Link>
                </header>
                {groupItems.length > 0 ? (
                  <div className={styles.groupList}>
                    {groupItems.map(itemCard)}
                  </div>
                ) : (
                  <div className={styles.emptyGroup}>
                    Nenhum {group.title.toLowerCase().slice(0, -1)} salvo ainda.
                  </div>
                )}
              </section>
            );
          })}
        </div>
      ) : (
        <div className={styles.list}>
          {items.map(itemCard)}
        </div>
      )}

      {totalPages > 1 && (
        <nav className={styles.pagination} aria-label='Páginas da biblioteca'>
          <button
            type='button'
            onClick={() => changePage(page - 1)}
            disabled={page === 0 || loading}
            aria-label='Página anterior'
          >
            <ArrowLeftIcon size={18} />
          </button>
          <span>
            Página {page + 1} de {totalPages}
          </span>
          <button
            type='button'
            onClick={() => changePage(page + 1)}
            disabled={page + 1 >= totalPages || loading}
            aria-label='Próxima página'
          >
            <ArrowRightIcon size={18} />
          </button>
        </nav>
      )}
    </main>
  );
}
