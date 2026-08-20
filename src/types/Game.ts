export type Game = {
  id: number;
  name: string;
  discountPercent: number;
  originalPrice: number | null;
  finalPrice: number;
  achievements: number;
  largeCapsuleImage: string;
  headerImage: string;
};
