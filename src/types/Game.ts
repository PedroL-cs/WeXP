export type Game = {
  id: string;
  steamAppId: number;
  name: string;
  shortDescription: string;
  detailedDescription: string;
  images: {
    cover: string;
    capsule: string;
    background: string;
    header: string;
    logo: string;
    hero: string;
  };
  releaseDate: string;
  createdAt: string;
  updatedAt: string;
};
