export type Guide = {
  id: string;
  content: string;
  version?: number;
  achievementId?: string;
  author?: string;
  createdAt?: string;
  updatedAt?: string;
  activeRevisionId: string | null;
  originalRevisionId: string | null;
  authorPublicId: string;
  creatorPublicId: string;
  creator: string;
  voteCount: number;
};
export type GuideRevision = {
  publicId: string;
  guidePublicId: string;
  authorPublicId: string;
  author: string;
  content: string;
  changeSummary?: string;
  status: 'PUBLISHED' | 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt?: string;
  baseRevisionId: string | null;
  voteCount: number;
  viewerHasVoted: boolean;
  isMain: boolean;
  isOriginal: boolean;
  isBasedOnCurrentMain: boolean;
};

export type CreateGuideRevisionRequest = {
  baseRevisionId: string;
  content: string;
  changeSummary: string;
};

export type RevisionVoteResponse = {
  revisionId: string;
  voteCount: number;
  viewerHasVoted: boolean;
  isMain: boolean;
  activeRevisionId: string;
  guideVersion: number;
};
