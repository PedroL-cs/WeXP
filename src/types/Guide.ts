export type Guide = {
  id: string;
  content: string;
  version?: number;
  achievementId?: string;
  authorUsername?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type GuideRevision = {
  publicId: string;
  guidePublicId: string;
  authorPublicId?: string;
  authorUsername?: string;
  content: string;
  changeSummary?: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | string;
  createdAt?: string;
};
