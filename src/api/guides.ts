import type {
  CreateGuideRevisionRequest,
  Guide,
  GuideRevision,
  RevisionVoteResponse,
} from '../types/Guide';
import type { PageResponse } from '../types/PageResponse';
import { throwApiError } from './apiError';
import { apiFetch } from './apiFetch';

export async function createAchievementGuide(
  achievementId: string,
  content: string,
) {
  const response = await apiFetch(
    `/api/v1/achievements/${achievementId}/guide`,
    { method: 'POST', body: JSON.stringify({ content }) },
  );
  if (!response.ok) throw new Error('Não foi possível criar o guia.');
  return response.json();
}

export async function getAchievementGuide(
  achievementId: string,
  signal?: AbortSignal,
): Promise<Guide | null> {
  const response = await apiFetch(
    `/api/v1/achievements/${achievementId}/guide`,
    { signal },
  );

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    return throwApiError(response, 'Não foi possível carregar o guia.');
  }

  return response.json();
}

export async function getGuide(
  guideId: string,
  signal?: AbortSignal,
): Promise<Guide> {
  const response = await apiFetch(
    `/api/v1/guides/${encodeURIComponent(guideId)}`,
    { signal },
  );
  if (!response.ok) {
    return throwApiError(response, 'Não foi possível carregar o guia.');
  }
  return response.json();
}

export async function createGuideRevision(
  guideId: string,
  request: CreateGuideRevisionRequest,
  signal?: AbortSignal,
): Promise<GuideRevision> {
  const response = await apiFetch(`/api/v1/guides/${guideId}/revisions`, {
    method: 'POST',
    body: JSON.stringify(request),
    signal,
  });
  if (!response.ok)
    return throwApiError(response, 'Não foi possível enviar a revisão.');
  return response.json();
}

export async function getGuideRevisions(
  guideId: string,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<PageResponse<GuideRevision>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  const response = await apiFetch(
    `/api/v1/guides/${guideId}/revisions?${params}`,
    { signal },
  );
  if (!response.ok)
    return throwApiError(response, 'Não foi possível carregar as revisões.');
  return response.json();
}

export async function getGuideRevision(
  revisionId: string,
  signal?: AbortSignal,
): Promise<GuideRevision> {
  const response = await apiFetch(
    `/api/v1/revisions/${encodeURIComponent(revisionId)}`,
    { signal },
  );
  if (!response.ok)
    return throwApiError(response, 'Não foi possível carregar esta versão.');
  return response.json();
}

export async function setRevisionUpvote(
  revisionId: string,
  upvote: boolean,
  signal?: AbortSignal,
): Promise<RevisionVoteResponse> {
  const response = await apiFetch(
    `/api/v1/revisions/${encodeURIComponent(revisionId)}/upvote`,
    {
      method: upvote ? 'PUT' : 'DELETE',
      signal,
    },
  );
  if (!response.ok)
    return throwApiError(
      response,
      'Não foi possível registrar seu voto. Tente novamente.',
    );
  return response.json();
}
