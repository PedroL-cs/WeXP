import { useCallback, useEffect, useRef, useState } from 'react';
import { getAchievement } from '../../api/achievements';
import { ApiError } from '../../api/apiError';
import {
  getAchievementGuide,
  getGuideRevision,
  getGuideRevisions,
  setRevisionUpvote,
} from '../../api/guides';
import { getCurrentUser, getPublicUser } from '../../api/users';
import type { Guide, GuideRevision } from '../../types/Guide';
import type { PageResponse } from '../../types/PageResponse';
import type { CurrentUser } from '../../types/User';

type AchievementInfo = {
  name: string;
  description?: string;
  icon?: string | null;
  icon_url?: string | null;
};

export type AuthorInfo = { username: string; avatar?: string };

function message(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

export function useGuideData(
  achievementId: string | undefined,
  isAuthenticated: boolean,
  tab: 'guide' | 'revisions',
  page: number,
  requestedRevisionId: string | null,
  showChanges: boolean,
) {
  const [achievement, setAchievement] = useState<AchievementInfo | null>(null);
  const [achievementError, setAchievementError] = useState('');
  const [guide, setGuide] = useState<Guide | null>(null);
  const [guideLoading, setGuideLoading] = useState(true);
  const [guideError, setGuideError] = useState('');
  const [revisionPage, setRevisionPage] =
    useState<PageResponse<GuideRevision> | null>(null);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');
  const [versions, setVersions] = useState<Record<string, GuideRevision>>({});
  const versionsRef = useRef<Record<string, GuideRevision>>({});
  const [selectionLoading, setSelectionLoading] = useState(false);
  const [selectionError, setSelectionError] = useState('');
  const [baseLoading, setBaseLoading] = useState(false);
  const [baseError, setBaseError] = useState('');
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState('');
  const [sessionExpired, setSessionExpired] = useState(false);
  const [authors, setAuthors] = useState<Record<string, AuthorInfo>>({});
  const authorCache = useRef<Record<string, AuthorInfo | null>>({});
  const [pendingVoteId, setPendingVoteId] = useState<string | null>(null);
  const voteRequest = useRef<AbortController | null>(null);
  const [voteError, setVoteError] = useState('');
  const [voteFeedback, setVoteFeedback] = useState('');
  const [reload, setReload] = useState(0);
  const [profileRetry, setProfileRetry] = useState(0);
  const [baseRetry, setBaseRetry] = useState(0);
  const generation = useRef(0);

  const mergeVersions = useCallback((items: GuideRevision[]) => {
    const next = { ...versionsRef.current };
    items.forEach(item => {
      next[item.publicId] = item;
    });
    versionsRef.current = next;
    setVersions(next);
  }, []);

  const refresh = useCallback(() => {
    generation.current += 1;
    setReload(value => value + 1);
  }, []);

  useEffect(() => {
    if (!achievementId) return;
    const controller = new AbortController();
    setAchievementError('');
    getAchievement(achievementId, controller.signal)
      .then(item => {
        if (!controller.signal.aborted) setAchievement(item);
      })
      .catch(error => {
        if (!controller.signal.aborted)
          setAchievementError(
            message(error, 'Não foi possível carregar a conquista.'),
          );
      });
    return () => controller.abort();
  }, [achievementId, reload]);

  useEffect(() => {
    if (!achievementId) {
      setGuideError('Conquista não encontrada.');
      setGuideLoading(false);
      return;
    }
    const controller = new AbortController();
    const currentGeneration = generation.current;
    const active = () =>
      !controller.signal.aborted && currentGeneration === generation.current;
    setGuideLoading(true);
    setGuideError('');
    getAchievementGuide(achievementId, controller.signal)
      .then(item => {
        if (active()) setGuide(item);
      })
      .catch(error => {
        if (active())
          setGuideError(message(error, 'Não foi possível carregar o guia.'));
      })
      .finally(() => {
        if (active()) setGuideLoading(false);
      });
    return () => controller.abort();
  }, [achievementId, reload]);

  useEffect(() => {
    const controller = new AbortController();
    setUser(null);
    setProfileError('');
    setSessionExpired(false);
    if (!isAuthenticated) {
      setProfileLoading(false);
      mergeVersions(
        Object.values(versionsRef.current).map(item => ({
          ...item,
          viewerHasVoted: false,
        })),
      );
      return;
    }
    setProfileLoading(true);
    getCurrentUser(controller.signal)
      .then(profile => {
        if (!controller.signal.aborted) setUser(profile);
      })
      .catch(error => {
        if (controller.signal.aborted) return;
        setProfileError(
          message(error, 'Não foi possível verificar seu perfil.'),
        );
        setSessionExpired(error instanceof ApiError && error.status === 401);
      })
      .finally(() => {
        if (!controller.signal.aborted) setProfileLoading(false);
      });
    return () => controller.abort();
  }, [isAuthenticated, profileRetry, mergeVersions]);

  const guideId = guide?.id;
  useEffect(() => {
    if (tab !== 'revisions' || !guideId) return;
    const controller = new AbortController();
    const currentGeneration = generation.current;
    const active = () =>
      !controller.signal.aborted && currentGeneration === generation.current;
    setHistoryLoading(true);
    setHistoryError('');
    getGuideRevisions(guideId, page, 20, controller.signal)
      .then(result => {
        if (!active()) return;
        mergeVersions(result.content);
        setRevisionPage(result);
      })
      .catch(error => {
        if (active())
          setHistoryError(
            message(error, 'Não foi possível carregar as revisões.'),
          );
      })
      .finally(() => {
        if (active()) setHistoryLoading(false);
      });
    return () => controller.abort();
  }, [tab, guideId, page, isAuthenticated, reload, mergeVersions]);

  const visiblePage = revisionPage?.page === page ? revisionPage : null;
  const selectedId =
    requestedRevisionId ??
    visiblePage?.content.find(item => item.isMain)?.publicId ??
    visiblePage?.content[0]?.publicId ??
    null;
  const selected =
    selectedId && versions[selectedId]?.guidePublicId === guideId
      ? versions[selectedId]
      : null;

  useEffect(() => {
    if (tab !== 'revisions' || !selectedId || !guideId) return;
    const controller = new AbortController();
    const currentGeneration = generation.current;
    const active = () =>
      !controller.signal.aborted && currentGeneration === generation.current;
    setSelectionLoading(true);
    setSelectionError('');
    getGuideRevision(selectedId, controller.signal)
      .then(item => {
        if (!active()) return;
        if (item.guidePublicId !== guideId)
          throw new Error('Esta versão não pertence ao guia desta conquista.');
        mergeVersions([item]);
      })
      .catch(error => {
        if (active())
          setSelectionError(
            message(error, 'Não foi possível carregar a versão selecionada.'),
          );
      })
      .finally(() => {
        if (active()) setSelectionLoading(false);
      });
    return () => controller.abort();
  }, [tab, selectedId, guideId, isAuthenticated, reload, mergeVersions]);

  const baseId = selected?.baseRevisionId;
  const base = baseId ? (versions[baseId] ?? null) : null;
  useEffect(() => {
    if (!showChanges || tab !== 'revisions' || !baseId || !guideId) return;
    setBaseError('');
    if (versionsRef.current[baseId]) {
      setBaseLoading(false);
      return;
    }
    const controller = new AbortController();
    const currentGeneration = generation.current;
    const active = () =>
      !controller.signal.aborted && currentGeneration === generation.current;
    setBaseLoading(true);
    getGuideRevision(baseId, controller.signal)
      .then(item => {
        if (!active()) return;
        if (item.guidePublicId !== guideId)
          throw new Error('A versão base não pertence a este guia.');
        mergeVersions([item]);
      })
      .catch(error => {
        if (active())
          setBaseError(
            message(
              error,
              'Não foi possível carregar a versão usada como base.',
            ),
          );
      })
      .finally(() => {
        if (active()) setBaseLoading(false);
      });
    return () => controller.abort();
  }, [
    baseId,
    guideId,
    tab,
    showChanges,
    baseRetry,
    isAuthenticated,
    reload,
    mergeVersions,
  ]);

  const authorIds = [
    ...new Set(
      [
        guide?.authorPublicId,
        guide?.creatorPublicId,
        selected?.authorPublicId,
        base?.authorPublicId,
        ...(visiblePage?.content.map(item => item.authorPublicId) ?? []),
      ].filter((id): id is string => Boolean(id)),
    ),
  ]
    .sort()
    .join(',');
  useEffect(() => {
    const ids = authorIds
      .split(',')
      .filter(id => id && !(id in authorCache.current));
    if (!ids.length) return;
    const controller = new AbortController();
    Promise.all(
      ids.map(async id => {
        try {
          const profile = await getPublicUser(id, controller.signal);
          return [
            id,
            { username: profile.username, avatar: profile.avatar },
          ] as const;
        } catch {
          return [id, null] as const;
        }
      }),
    ).then(entries => {
      if (controller.signal.aborted) return;
      entries.forEach(([id, profile]) => {
        authorCache.current[id] = profile;
      });
      setAuthors(
        Object.fromEntries(
          Object.entries(authorCache.current).filter(
            (entry): entry is [string, AuthorInfo] => entry[1] !== null,
          ),
        ),
      );
    });
    return () => controller.abort();
  }, [authorIds]);

  useEffect(() => {
    setPendingVoteId(null);
    return () => voteRequest.current?.abort();
  }, [isAuthenticated]);

  async function toggleVote(revision: GuideRevision) {
    if (
      voteRequest.current ||
      !user ||
      !isAuthenticated ||
      revision.authorPublicId === user.publicId
    )
      return;
    const controller = new AbortController();
    voteRequest.current = controller;
    setPendingVoteId(revision.publicId);
    setVoteError('');
    setVoteFeedback('');
    try {
      const response = await setRevisionUpvote(
        revision.publicId,
        !revision.viewerHasVoted,
        controller.signal,
      );
      if (controller.signal.aborted) return;
      mergeVersions(
        Object.values(versionsRef.current).map(item => ({
          ...item,
          isMain: item.publicId === response.activeRevisionId,
          isBasedOnCurrentMain:
            item.baseRevisionId === response.activeRevisionId,
          ...(item.publicId === response.revisionId
            ? {
                voteCount: response.voteCount,
                viewerHasVoted: response.viewerHasVoted,
              }
            : {}),
        })),
      );
      const changedMain = response.activeRevisionId !== guide?.activeRevisionId;
      setVoteFeedback(
        `${response.viewerHasVoted ? 'Voto registrado.' : 'Voto retirado.'}${changedMain ? ' A versão principal mudou.' : ''}`,
      );
      refresh();
    } catch (error) {
      if (controller.signal.aborted) return;
      setVoteError(message(error, 'Não foi possível registrar seu voto.'));
      if (error instanceof ApiError && error.status === 401) {
        setSessionExpired(true);
        setUser(null);
      }
    } finally {
      if (voteRequest.current === controller) voteRequest.current = null;
      if (!controller.signal.aborted) setPendingVoteId(null);
    }
  }

  return {
    achievement,
    achievementError,
    guide,
    guideLoading,
    guideError,
    revisionPage: visiblePage,
    pagination: revisionPage,
    versions,
    selected,
    selectedId,
    selectionLoading,
    selectionError,
    historyLoading,
    historyError,
    base,
    baseLoading,
    baseError,
    authors,
    user,
    profileLoading,
    profileError,
    sessionExpired,
    pendingVoteId,
    voteError,
    voteFeedback,
    toggleVote,
    refresh,
    retryProfile: () => setProfileRetry(value => value + 1),
    retryBase: () => setBaseRetry(value => value + 1),
  };
}
