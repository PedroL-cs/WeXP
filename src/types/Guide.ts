export type Guide = {
  id: string;
  content: string;
  version?: number;
  achievementId?: string;
  author?: string;
  createdAt?: string;
  updatedAt?: string;
};
export type GuideRevision = {
  publicId: string;
  guidePublicId: string;
  authorPublicId?: string;
  content: string;
  changeSummary?: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | string;
  createdAt?: string;
};
