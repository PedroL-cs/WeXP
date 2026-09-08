import type { Achievement } from './Achievement';

export interface GameImage {
  cover: string;
  capsule: string;
  background: string;
  header: string;
  logo: string;
  hero: string;
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
