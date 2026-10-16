export type Achievement = {
  id: string;
  name: string;
  description: string;
  hidden: boolean;
  icon: string | null;
  game?: {
    id: string;
    name: string;
    steamAppId: number;
  };
};

export type AchievementDetails = Achievement & {
  isHidden: boolean;
  game: NonNullable<Achievement['game']>;
};
