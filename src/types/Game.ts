import type { Achievement } from './Achievement';

export interface GameImage {
  cover: string | null;
  capsule: string | null;
  background: string | null;
  header: string | null;
  logo: string | null;
  hero: string | null;
}

export interface GameCategory {
  id: string;
  name: string;
}

export interface GameGenre {
  id: string;
  name: string;
}

export interface Game {
  id: string;
  steamAppId: number;
  name: string;
  shortDescription: string;
  detailedDescription: string;
  viewsCount: number;
  images: GameImage;
  releaseDate: string;
  categories: GameCategory[];
  genres: GameGenre[];
  achievements: Achievement[];
  createdAt: string;
  updatedAt: string;
}
