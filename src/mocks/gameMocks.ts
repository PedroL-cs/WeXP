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

export const mockGame: Game = {
  id: '1',
  steamAppId: 3764200,
  name: 'Resident Evil Requiem',
  shortDescription:
    'Requiem for the dead. Nightmare for the living. Prepare to escape death in a heart-stopping experience that will chill you to your core.',
  detailedDescription: '',
  images: {
    cover: '',
    capsule:
      'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3764200/64f506b9b6210df6055136e7017f2082957e5b22/capsule_231x87.jpg?t=1779840172',
    background:
      'https://store.akamai.steamstatic.com/images/storepagebackground/app/3764200?t=1779840172',
    header:
      'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3764200/ce5437442768e38eb575f205ab9397d0264017b0/header.jpg?t=1779840172',
    logo: '',
    hero: 'https://shared.steamstatic.com/store_item_assets/steam/apps/3764200/library_hero.jpg',
  },
  releaseDate: '2026-02-26',
  createdAt: '2026-02-26T00:00:00Z',
  updatedAt: '2026-02-26T00:00:00Z',
};
