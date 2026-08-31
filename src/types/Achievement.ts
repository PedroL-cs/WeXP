export type Achievement = {
  id: number;
  game_id: number;
  steam_api_name: string;
  name: string;
  description: string;
  icon: string;
  hidden: boolean;
};

export type PaginatedResponse<T> = {
  total: number;
  items: T[];
};
