export type SavedResourceType = 'GAME' | 'ACHIEVEMENT' | 'GUIDE';

export type SavedItem = {
  type: SavedResourceType;
  resourceId: string;
  savedAt: string;
};

export type SavedStatus = {
  type: SavedResourceType;
  resourceId: string;
  saved: boolean;
  savedAt: string | null;
};
